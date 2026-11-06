package org.hit.chiikaiwabe.admin;

import jakarta.servlet.FilterChain;
import org.hit.chiikaiwabe.config.WebSocketConfig;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.*;
import org.hit.chiikaiwabe.domain.enums.Role;
import org.hit.chiikaiwabe.filter.GlobalRateLimitFilter;
import org.hit.chiikaiwabe.interceptor.WebSocketRateLimitInterceptor;
import org.hit.chiikaiwabe.repository.*;
import org.hit.chiikaiwabe.security.*;
import org.hit.chiikaiwabe.security.jwt.*;
import org.hit.chiikaiwabe.service.CustomUserDetailsService;
import org.hit.chiikaiwabe.service.impl.CustomUserDetailsServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.data.redis.*;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.*;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.*;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Exercise actual SockJS/STOMP, HTTP security and outbound session decoration. */
@SpringBootTest(classes = WebSocketProtocolTest.App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.config.import=", "jwt.secret=Y2hpaWlrYWl3YS10ZXN0LXNlY3JldC1rZXktZm9yLWp3dC1vbmx5",
        "jwt.access.expiration_time=30", "jwt.refresh.expiration_time=60", "springdoc.api-docs.enabled=false"})
class WebSocketProtocolTest {
    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class,
        RedisAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class,
        org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration.class})
    @Import({WebSocketConfig.class, WebSecurityConfig.class, JwtAuthenticationFilter.class,
        JwtHandshakeInterceptor.class, JwtChannelInterceptor.class, AccountStatusWebSocketGuard.class,
        AccessTokenAuthenticator.class, WebSocketAuthorizationService.class, JwtTokenProvider.class})
    static class App {
        @Bean UserRepository users() { return mock(UserRepository.class); }
        @Bean ConversationMemberRepository members() { return mock(ConversationMemberRepository.class); }
        @Bean StringRedisTemplate redis() { return mock(StringRedisTemplate.class); }
        @Bean CustomUserDetailsService details(UserRepository users) { return new CustomUserDetailsServiceImpl(users); }
        @Bean WebSocketRateLimitInterceptor rateLimit() {
            var rate = mock(WebSocketRateLimitInterceptor.class);
            when(rate.preSend(any(), any())).thenAnswer(inv -> inv.getArgument(0));
            return rate;
        }
        @Bean GlobalRateLimitFilter httpRateLimit() throws Exception {
            var rate = mock(GlobalRateLimitFilter.class);
            doAnswer(inv -> { ((FilterChain) inv.getArgument(2)).doFilter(inv.getArgument(0), inv.getArgument(1)); return null; })
                    .when(rate).doFilter(any(), any(), any());
            return rate;
        }
    }

    @LocalServerPort int port;
    @Autowired JwtTokenProvider tokens;
    @Autowired UserRepository users;
    @Autowired ConversationMemberRepository members;
    @Autowired StringRedisTemplate redis;
    @Autowired SimpMessagingTemplate broker;
    WebSocketStompClient client;
    StompSession session;
    User user;
    String token;
    Handler handler;

    static class Handler extends StompSessionHandlerAdapter {
        CompletableFuture<Throwable> failed = new CompletableFuture<>();
        @Override public void handleTransportError(StompSession session, Throwable ex) { failed.complete(ex); }
        @Override public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable ex) {
            failed.complete(ex);
        }
        @Override public Type getPayloadType(StompHeaders headers) { return Map.class; }
        @Override public void handleFrame(StompHeaders headers, Object payload) {
            failed.complete(new IllegalStateException("STOMP ERROR"));
        }
    }

    @BeforeEach void setup() {
        reset(users, members, redis);
        user = User.builder().id("protocol-user").username("protocol-user").role(Role.USER).userstatus(UserStatus.ACTIVE).build();
        when(users.findById(user.getId())).thenReturn(Optional.of(user));
        when(members.existsActiveMember("conversation-1", user.getId())).thenReturn(true);
        token = tokens.generateToken(UserPrincipal.create(user), false);
        client = new WebSocketStompClient(new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        client.setMessageConverter(new MappingJackson2MessageConverter());
        client.setDefaultHeartbeat(new long[]{0, 0});
        handler = new Handler();
    }

    @AfterEach void cleanup() {
        if (session != null && session.isConnected()) {
            try { session.disconnect(); } catch (RuntimeException ignored) { }
        }
        client.stop();
    }

    StompSession connect(String connectToken) throws Exception {
        var http = new WebSocketHttpHeaders();
        http.setOrigin("http://localhost:3000");
        StompHeaders headers = new StompHeaders();
        if (connectToken != null) headers.set("Authorization", "Bearer " + connectToken);
        return client.connectAsync("http://127.0.0.1:" + port + "/ws?token=" + token, http, headers, handler)
                .get(10, TimeUnit.SECONDS);
    }

    @Test void allowedMemberReceivesMessageAndLogoutStopsDelivery() throws Exception {
        session = connect(null);
        BlockingQueue<Object> messages = new LinkedBlockingQueue<>();
        session.subscribe("/topic/conversation.conversation-1", new StompFrameHandler() {
            @Override public Type getPayloadType(StompHeaders headers) { return Map.class; }
            @Override public void handleFrame(StompHeaders headers, Object payload) { messages.add(payload); }
        });
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            broker.convertAndSend("/topic/conversation.conversation-1", Map.of("event", "allowed"));
            assertThat(messages.poll(200, TimeUnit.MILLISECONDS)).isNotNull();
        });
        messages.clear();
        when(redis.hasKey("BLACKLIST:" + tokens.extractJtiFromJwt(token))).thenReturn(true);
        broker.convertAndSend("/topic/conversation.conversation-1", Map.of("event", "forbidden"));
        assertThat(handler.failed.get(5, TimeUnit.SECONDS)).isNotNull();
        assertThat(messages.poll(200, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test void foreignConversationSubscriptionIsRejected() throws Exception {
        session = connect(null);
        session.subscribe("/topic/conversation.someone-else", new StompFrameHandler() {
            @Override public Type getPayloadType(StompHeaders headers) { return Map.class; }
            @Override public void handleFrame(StompHeaders headers, Object payload) { fail("Unauthorized delivery"); }
        });
        assertThat(handler.failed.get(5, TimeUnit.SECONDS)).isNotNull();
    }

    @Test void connectIdentityCannotDifferFromHandshake() {
        User other = User.builder().id("other").username("other").role(Role.USER).userstatus(UserStatus.ACTIVE).build();
        when(users.findById("other")).thenReturn(Optional.of(other));
        String otherToken = tokens.generateToken(UserPrincipal.create(other), false);
        assertThatThrownBy(() -> connect(otherToken)).isInstanceOf(ExecutionException.class);
    }
    @Test void transportWithoutTokenCannotConnect() {
        token = "";
        assertThatThrownBy(() -> connect(null)).isInstanceOf(ExecutionException.class);
    }

}
