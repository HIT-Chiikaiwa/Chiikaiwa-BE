package org.hit.chiikaiwabe.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token != null && jwtTokenProvider.validateToken(token)) {
                String jti = jwtTokenProvider.extractJtiFromJwt(token);
                if (Boolean.TRUE.equals(redisTemplate.hasKey("BLACKLIST:" + jti))) {
                    log.warn("WebSocket handshake rejected: token is blacklisted");
                    return false;
                }
                String userId = jwtTokenProvider.extractSubjectFromJwt(token);
                attributes.put("userId", userId);
                log.info("WebSocket handshake success for user: {}", userId);
                return true;
            }
        }
        log.warn("WebSocket handshake rejected: missing or invalid token");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
