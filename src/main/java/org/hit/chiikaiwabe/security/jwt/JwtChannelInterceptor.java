package org.hit.chiikaiwabe.security.jwt;

import org.hit.chiikaiwabe.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String userId = null;

            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    userId = jwtTokenProvider.extractSubjectFromJwt(token);
                } else {
                    log.warn("STOMP CONNECT rejected: invalid Bearer token");
                    throw new IllegalArgumentException("Invalid JWT token");
                }
            }

            if (userId == null) {
                Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                if (sessionAttributes != null && sessionAttributes.containsKey("userId")) {
                    userId = (String) sessionAttributes.get("userId");
                }
            }

            if (userId == null) {
                log.warn("STOMP CONNECT rejected: no valid authentication found");
                throw new IllegalArgumentException("Authentication required for WebSocket connection");
            }

            var userDetails = customUserDetailsService.loadUserById(userId);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userId, null, userDetails.getAuthorities());
            accessor.setUser(auth);
            log.info("STOMP CONNECT authenticated for user: {}", userId);
        }
        if (accessor != null && (StompCommand.SEND.equals(accessor.getCommand())
                || StompCommand.SUBSCRIBE.equals(accessor.getCommand()))) {
            if (accessor.getUser() == null) {
                throw new IllegalArgumentException("Authentication required for WebSocket message");
            }
            customUserDetailsService.loadUserById(accessor.getUser().getName());
        }
        return message;
    }
}
