package org.hit.chiikaiwabe.security.jwt;

import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.exception.UnauthorizedException;
import org.hit.chiikaiwabe.security.AccessTokenAuthenticator;
import org.hit.chiikaiwabe.security.WebSocketAuthorizationService;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {
    private static final Set<StompCommand> ALLOWED = Set.of(StompCommand.CONNECT, StompCommand.STOMP,
            StompCommand.SEND, StompCommand.SUBSCRIBE, StompCommand.UNSUBSCRIBE,
            StompCommand.ACK, StompCommand.NACK, StompCommand.DISCONNECT);
    private final AccessTokenAuthenticator authenticator;
    private final WebSocketAuthorizationService authorization;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) throw new UnauthorizedException(ErrorMessage.UNAUTHORIZED);
        var command = accessor.getCommand();
        if (command == StompCommand.DISCONNECT) return message;
        if (command != null && !ALLOWED.contains(command)) throw new UnauthorizedException(ErrorMessage.UNAUTHORIZED);
        var attributes = accessor.getSessionAttributes();
        if (attributes == null || !(attributes.get("userId") instanceof String handshakeUser)
                || !(attributes.get(JwtHandshakeInterceptor.ACCESS_TOKEN) instanceof String storedToken)) {
            throw new UnauthorizedException(ErrorMessage.UNAUTHORIZED);
        }
        boolean connecting = command == StompCommand.CONNECT || command == StompCommand.STOMP;
        String token = storedToken;
        if (connecting) {
            String header = accessor.getFirstNativeHeader("Authorization");
            if (header != null) {
                if (!header.startsWith("Bearer ")) throw new UnauthorizedException(ErrorMessage.UNAUTHORIZED);
                token = header.substring(7);
            }
        }
        var principal = authenticator.authenticate(token);
        if (!handshakeUser.equals(principal.getId())) throw new UnauthorizedException(ErrorMessage.UNAUTHORIZED);
        if (connecting) {
            attributes.put(JwtHandshakeInterceptor.ACCESS_TOKEN, token);
            accessor.setUser(new UsernamePasswordAuthenticationToken(principal.getId(), null, principal.getAuthorities()));
        } else if (accessor.getUser() == null || !principal.getId().equals(accessor.getUser().getName())) {
            throw new UnauthorizedException(ErrorMessage.UNAUTHORIZED);
        }
        if (command == StompCommand.SUBSCRIBE) authorization.authorizeSubscription(principal.getId(), accessor.getDestination());
        if (command == StompCommand.SEND) authorization.authorizeSend(accessor.getDestination());
        return message;
    }
}
