package org.hit.chiikaiwabe.config;

import org.hit.chiikaiwabe.security.jwt.JwtChannelInterceptor;
import org.hit.chiikaiwabe.security.AccountStatusWebSocketGuard;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.hit.chiikaiwabe.security.jwt.JwtHandshakeInterceptor;
import org.hit.chiikaiwabe.interceptor.WebSocketRateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AccountStatusWebSocketGuard accountStatusWebSocketGuard;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final JwtChannelInterceptor jwtChannelInterceptor;
    private final WebSocketRateLimitInterceptor webSocketRateLimitInterceptor;

    @org.springframework.context.annotation.Bean
    public ThreadPoolTaskScheduler stompHeartbeatThread() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("wss-heartbeat-thread-");
        taskScheduler.initialize();
        return taskScheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(stompHeartbeatThread());
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOriginPatterns("https://api.chiikaiwa.me", "https://chiikaiwa.me", "http://localhost:3000", "http://localhost:5173")
                .withSockJS()
                .setDisconnectDelay(60000);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(accountStatusWebSocketGuard);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(accountStatusWebSocketGuard);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor, webSocketRateLimitInterceptor);
    }
}
