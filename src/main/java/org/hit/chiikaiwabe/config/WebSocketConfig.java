package org.hit.chiikaiwabe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kích hoạt một in-memory message broker đơn giản
        // Các client subscribe các kênh bắt đầu bằng "/topic" hoặc "/queue"
        config.enableSimpleBroker("/topic", "/queue");
        
        // Tiền tố cho các request gửi từ client lên server (ví dụ /app/chat.sendMessage)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký endpoint cho client kết nối WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cho phép mọi origin truy cập
                .withSockJS(); // Cung cấp fallback cho browser không hỗ trợ WebSocket
    }
}
