package org.hit.chiikaiwabe.admin;

import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.Role;
import org.hit.chiikaiwabe.domain.enums.UserStatus;
import org.hit.chiikaiwabe.exception.UnauthorizedException;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.security.jwt.*;
import org.hit.chiikaiwabe.service.impl.CustomUserDetailsServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.WebSocketHandler;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountLockWebSocketTest {
  UserRepository repository = mock(UserRepository.class);
  JwtTokenProvider tokens = mock(JwtTokenProvider.class);
  StringRedisTemplate redis = mock(StringRedisTemplate.class);
  CustomUserDetailsServiceImpl details = new CustomUserDetailsServiceImpl(repository);
  JwtChannelInterceptor interceptor = new JwtChannelInterceptor(details, tokens);
  User user;

  @BeforeEach void setup() {
    user = User.builder().id("user-1").username("alice").role(Role.USER)
        .userstatus(UserStatus.ACTIVE).build();
    when(repository.findById("user-1")).thenReturn(Optional.of(user));
  }

  Message<byte[]> frame(StompCommand command) {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
    accessor.setUser(new UsernamePasswordAuthenticationToken("user-1", null));
    accessor.setSessionAttributes(new HashMap<>(Map.of("userId", "user-1")));
    accessor.setLeaveMutable(true);
    return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
  }

  @Test void activeUserCanConnectButLockedUserCannot() {
    assertThat(interceptor.preSend(frame(StompCommand.CONNECT), null)).isNotNull();
    user.setUserstatus(UserStatus.LOCKED);
    assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.CONNECT), null))
        .isInstanceOf(UnauthorizedException.class);
  }

  @Test void alreadyConnectedUserCannotSendOrSubscribeAfterLock() {
    Message<byte[]> send = frame(StompCommand.SEND);
    assertThat(interceptor.preSend(send, null)).isSameAs(send);
    user.setUserstatus(UserStatus.LOCKED);
    assertThatThrownBy(() -> interceptor.preSend(send, null)).isInstanceOf(UnauthorizedException.class);
    assertThatThrownBy(() -> interceptor.preSend(frame(StompCommand.SUBSCRIBE), null))
        .isInstanceOf(UnauthorizedException.class);
  }

  @Test void existingSubscriberCannotReceiveMessagesAfterLock() throws Exception {
    var guard = new org.hit.chiikaiwabe.security.AccountStatusWebSocketGuard(details);
    WebSocketHandler delegate = mock(WebSocketHandler.class);
    var handler = guard.decorate(delegate);
    org.springframework.web.socket.WebSocketSession session = mock(org.springframework.web.socket.WebSocketSession.class);
    when(session.getId()).thenReturn("session-1");
    when(session.getAttributes()).thenReturn(new HashMap<>(Map.of("userId", "user-1")));
    handler.afterConnectionEstablished(session);
    var accessor = org.springframework.messaging.simp.SimpMessageHeaderAccessor.create(
        org.springframework.messaging.simp.SimpMessageType.MESSAGE);
    accessor.setSessionId("session-1");
    var message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    assertThat(guard.preSend(message, null)).isSameAs(message);
    user.setUserstatus(UserStatus.LOCKED);
    assertThat(guard.preSend(message, null)).isNull();
    verify(session).close(org.springframework.web.socket.CloseStatus.POLICY_VIOLATION);
    handler.afterConnectionClosed(session, org.springframework.web.socket.CloseStatus.NORMAL);
    user.setUserstatus(UserStatus.ACTIVE);
    assertThat(guard.preSend(message, null)).isNull();
  }

  @Test void handshakeRejectsLockedAccountWithValidToken() {
    user.setUserstatus(UserStatus.LOCKED);
    when(tokens.validateToken("access")).thenReturn(true);
    when(tokens.extractJtiFromJwt("access")).thenReturn("jti");
    when(tokens.extractSubjectFromJwt("access")).thenReturn("user-1");
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter("token", "access");
    var handshake = new JwtHandshakeInterceptor(details, tokens, redis);
    Map<String, Object> attributes = new HashMap<>();
    assertThat(handshake.beforeHandshake(new ServletServerHttpRequest(request),
        new ServletServerHttpResponse(new MockHttpServletResponse()), mock(WebSocketHandler.class), attributes))
        .isFalse();
    assertThat(attributes).doesNotContainKey("userId");
  }
}
