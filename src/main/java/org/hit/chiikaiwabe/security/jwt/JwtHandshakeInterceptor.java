package org.hit.chiikaiwabe.security.jwt;

import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.security.AccessTokenAuthenticator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    public static final String ACCESS_TOKEN = "wsAccessToken";
    private final AccessTokenAuthenticator authenticator;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler handler, Map<String, Object> attributes) {
        String token = null;
        if (request instanceof ServletServerHttpRequest servletRequest) {
            token = servletRequest.getServletRequest().getParameter("token");
        }
        if (token == null) {
            String bearer = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (bearer != null && bearer.startsWith("Bearer ")) token = bearer.substring(7);
        }
        try {
            if (token == null) return false;
            var user = authenticator.authenticate(token);
            attributes.put("userId", user.getId());
            attributes.put(ACCESS_TOKEN, token);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler handler, Exception exception) { }
}
