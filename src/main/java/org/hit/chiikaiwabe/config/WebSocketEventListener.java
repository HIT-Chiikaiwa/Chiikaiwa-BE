package org.hit.chiikaiwabe.config;

import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.service.OnlineStatusService;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final OnlineStatusService onlineStatusService;
    private final RedisTemplate<String, Object> redisTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event){
        Principal principal = event.getUser();
        if(principal != null){
            onlineStatusService.setOnline(principal.getName());
            redisTemplate.opsForValue().set("user:ws-session:" + principal.getName(),
                    event.getMessage().getHeaders().get("simpSessionId").toString());
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event){
        Principal principal = event.getUser();
        if(principal != null){
            onlineStatusService.setOffline(principal.getName());
            redisTemplate.delete("user:ws-session:" + principal.getName());
        }
    }
}
