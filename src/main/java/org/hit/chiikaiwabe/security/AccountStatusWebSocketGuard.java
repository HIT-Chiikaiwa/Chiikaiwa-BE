package org.hit.chiikaiwabe.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hit.chiikaiwabe.service.CustomUserDetailsService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Checks already subscribed sessions before delivering application messages. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountStatusWebSocketGuard implements WebSocketHandlerDecoratorFactory, ChannelInterceptor {

    private final CustomUserDetailsService customUserDetailsService;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public WebSocketHandler decorate(WebSocketHandler handler) {
        return new WebSocketHandlerDecorator(handler) {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                sessions.put(session.getId(), session);
                try {
                    super.afterConnectionEstablished(session);
                } catch (Exception ex) {
                    sessions.remove(session.getId());
                    throw ex;
                }
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                sessions.remove(session.getId());
                super.afterConnectionClosed(session, closeStatus);
            }
        };
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        if (SimpMessageHeaderAccessor.getMessageType(message.getHeaders()) != SimpMessageType.MESSAGE) {
            return message;
        }
        String sessionId = SimpMessageHeaderAccessor.getSessionId(message.getHeaders());
        WebSocketSession session = sessionId == null ? null : sessions.get(sessionId);
        if (session == null) {
            return null;
        }
        Object userId = session.getAttributes().get("userId");
        try {
            if (!(userId instanceof String)) {
                return null;
            }
            customUserDetailsService.loadUserById((String) userId);
            return message;
        } catch (RuntimeException ex) {
            try {
                session.close(CloseStatus.POLICY_VIOLATION);
            } catch (IOException closeException) {
                log.warn("Could not close unavailable account WebSocket session: {}", sessionId);
            }
            return null;
        }
    }
}
