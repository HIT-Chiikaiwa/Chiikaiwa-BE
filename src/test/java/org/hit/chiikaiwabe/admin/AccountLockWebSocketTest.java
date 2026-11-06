package org.hit.chiikaiwabe.admin;

import org.hit.chiikaiwabe.controller.ChatWebSocketController;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.*;
import org.hit.chiikaiwabe.exception.*;
import org.hit.chiikaiwabe.repository.*;
import org.hit.chiikaiwabe.security.*;
import org.hit.chiikaiwabe.security.jwt.*;
import org.hit.chiikaiwabe.service.*;
import org.hit.chiikaiwabe.service.impl.CustomUserDetailsServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.*;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.*;

import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountLockWebSocketTest {
    UserRepository repository = mock(UserRepository.class);
    ConversationMemberRepository members = mock(ConversationMemberRepository.class);
    JwtTokenProvider tokens = new JwtTokenProvider();
    StringRedisTemplate redis = mock(StringRedisTemplate.class);
    CustomUserDetailsServiceImpl details = new CustomUserDetailsServiceImpl(repository);
    AccessTokenAuthenticator authenticator = new AccessTokenAuthenticator(tokens, redis, details);
    WebSocketAuthorizationService authorization = new WebSocketAuthorizationService(members);
    JwtChannelInterceptor interceptor = new JwtChannelInterceptor(authenticator, authorization);
    User user;
    String token;

    @BeforeEach void setup() {
        ReflectionTestUtils.setField(tokens, "SECRET_KEY", "Y2hpaWlrYWl3YS10ZXN0LXNlY3JldC1rZXktZm9yLWp3dC1vbmx5");
        ReflectionTestUtils.setField(tokens, "EXPIRATION_TIME_ACCESS_TOKEN", 30);
        ReflectionTestUtils.setField(tokens, "EXPIRATION_TIME_REFRESH_TOKEN", 60);
        user = User.builder().id("user-1").username("alice").role(Role.USER).userstatus(UserStatus.ACTIVE).build();
        when(repository.findById("user-1")).thenReturn(Optional.of(user));
        when(members.existsActiveMember("conversation-1", "user-1")).thenReturn(true);
        token = tokens.generateToken(UserPrincipal.create(user), false);
    }

    Message<byte[]> frame(StompCommand command, String destination, String connectToken) {
        var accessor = StompHeaderAccessor.create(command);
        accessor.setUser(new UsernamePasswordAuthenticationToken("user-1", null));
        accessor.setDestination(destination);
        accessor.setSessionAttributes(new HashMap<>(Map.of("userId", "user-1", JwtHandshakeInterceptor.ACCESS_TOKEN, token)));
        if (connectToken != null) accessor.setNativeHeader("Authorization", "Bearer " + connectToken);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test void activeUserCanConnectButLockedUserCannot() {
        assertThat(interceptor.preSend(frame(StompCommand.CONNECT, null, null), null)).isNotNull();
        user.setUserstatus(UserStatus.LOCKED);
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT, null, null), null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test void connectedUserCannotSendOrSubscribeAfterLock() {
        var send = frame(StompCommand.SEND, "/app/chat.send", null);
        assertThat(interceptor.preSend(send, null)).isSameAs(send);
        user.setUserstatus(UserStatus.LOCKED);
        assertThatThrownBy(() -> interceptor.preSend(send, null)).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/conversation.conversation-1", null), null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test void connectMustMatchHandshakeIdentity() {
        User other = User.builder().id("other").username("bob").role(Role.USER).userstatus(UserStatus.ACTIVE).build();
        when(repository.findById("other")).thenReturn(Optional.of(other));
        String otherToken = tokens.generateToken(UserPrincipal.create(other), false);
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT, null, otherToken), null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test void refreshTokenCannotAuthenticateWebSocket() {
        String refresh = tokens.generateToken(UserPrincipal.create(user), true);
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT, null, refresh), null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test void revokedAndExpiredAccessTokenCannotConnectOrContinueSending() {
        when(redis.hasKey("BLACKLIST:" + tokens.extractJtiFromJwt(token))).thenReturn(true);
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT, null, null), null))
                .isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SEND, "/app/chat.send", null), null))
                .isInstanceOf(UnauthorizedException.class);
        ReflectionTestUtils.setField(tokens, "EXPIRATION_TIME_ACCESS_TOKEN", -1);
        String expired = tokens.generateToken(UserPrincipal.create(user), false);
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT, null, expired), null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test void subscriptionsRequireActiveMembershipAndRejectRawUserQueues() {
        assertThat(interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/topic/conversation.conversation-1", null), null)).isNotNull();
        for (String destination : new String[]{"/topic/conversation.other", "/queue/notifications-user-session",
                "/user/other/queue/notifications", "/topic/unknown"}) {
            assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SUBSCRIBE, destination, null), null))
                    .isInstanceOf(ForbiddenException.class);
        }
        assertThat(interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/user/queue/notifications", null), null)).isNotNull();
        assertThat(interceptor.preSend(frame(StompCommand.SUBSCRIBE, "/user/queue/friendship", null), null)).isNotNull();
    }

    @Test void sendingDirectlyToBrokerIsForbidden() {
        assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SEND, "/topic/conversation.conversation-1", null), null))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test void typingRequiresConversationMembership() {
        OnlineStatusService online = mock(OnlineStatusService.class);
        var controller = new ChatWebSocketController(authorization, mock(MessageActionService.class), online);
        assertThatThrownBy(() -> controller.typing(Map.of("conversationId", "other"), () -> "user-1"))
                .isInstanceOf(ForbiddenException.class);
        verifyNoInteractions(online);
        controller.typing(Map.of("conversationId", "conversation-1"), () -> "user-1");
        verify(online).setTyping("conversation-1", "user-1");
    }

    @Test void subscriberLosingMembershipCannotReceiveFurtherMessages() throws Exception {
        var guard = new AccountStatusWebSocketGuard(authenticator, authorization);
        var handler = guard.decorate(mock(WebSocketHandler.class));
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("session-1");
        when(session.getAttributes()).thenReturn(new HashMap<>(Map.of("userId", "user-1", JwtHandshakeInterceptor.ACCESS_TOKEN, token)));
        handler.afterConnectionEstablished(session);
        var accessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        accessor.setSessionId("session-1");
        accessor.setDestination("/topic/conversation.conversation-1");
        var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        assertThat(guard.preSend(message, null)).isSameAs(message);
        when(members.existsActiveMember("conversation-1", "user-1")).thenReturn(false);
        assertThat(guard.preSend(message, null)).isNull();
        verify(session).close(CloseStatus.POLICY_VIOLATION);
    }

    @Test void subscriberCannotReceiveAfterLockOrLogout() throws Exception {
        var guard = new AccountStatusWebSocketGuard(authenticator, authorization);
        var handler = guard.decorate(mock(WebSocketHandler.class));
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("session-1");
        when(session.getAttributes()).thenReturn(new HashMap<>(Map.of("userId", "user-1", JwtHandshakeInterceptor.ACCESS_TOKEN, token)));
        handler.afterConnectionEstablished(session);
        var accessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        accessor.setSessionId("session-1");
        accessor.setDestination("/queue/notifications-usersession-1");
        var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        user.setUserstatus(UserStatus.LOCKED);
        assertThat(guard.preSend(message, null)).isNull();
        user.setUserstatus(UserStatus.ACTIVE);
        when(redis.hasKey("BLACKLIST:" + tokens.extractJtiFromJwt(token))).thenReturn(true);
        assertThat(guard.preSend(message, null)).isNull();
        verify(session, times(2)).close(CloseStatus.POLICY_VIOLATION);
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
        assertThat(guard.preSend(message, null)).isNull();
    }

    @Test void handshakeAcceptsHeaderAndRejectsLockedOrRevokedAccount() {
        var handshake = new JwtHandshakeInterceptor(authenticator);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        var response = new ServletServerHttpResponse(new MockHttpServletResponse());
        var attributes = new HashMap<String, Object>();
        assertThat(handshake.beforeHandshake(new ServletServerHttpRequest(request), response, mock(WebSocketHandler.class), attributes)).isTrue();
        assertThat(attributes.get("userId")).isEqualTo("user-1");
        user.setUserstatus(UserStatus.LOCKED);
        assertThat(handshake.beforeHandshake(new ServletServerHttpRequest(request), response, mock(WebSocketHandler.class), new HashMap<>())).isFalse();
        user.setUserstatus(UserStatus.ACTIVE);
        when(redis.hasKey("BLACKLIST:" + tokens.extractJtiFromJwt(token))).thenReturn(true);
        assertThat(handshake.beforeHandshake(new ServletServerHttpRequest(request), response, mock(WebSocketHandler.class), new HashMap<>())).isFalse();
    }
}
