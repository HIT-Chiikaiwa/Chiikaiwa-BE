package org.hit.chiikaiwabe.controller;

import org.hit.chiikaiwabe.domain.dto.request.SendMessageRequestDto;
import org.hit.chiikaiwabe.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatWebSocketController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequestDto dto, Principal principal) {
        String senderId = principal.getName();
        log.info("WebSocket message from user {} to conversation {}", senderId, dto.getConversationId());
        chatService.sendMessage(senderId, dto);
    }

    @MessageMapping("/chat.read")
    public void markAsRead(@Payload java.util.Map<String, String> payload, Principal principal) {
        String userId = principal.getName();
        String conversationId = payload.get("conversationId");
        chatService.markAsRead(conversationId, userId);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload java.util.Map<String, String> payload, Principal principal) {
        String userId = principal.getName();
        String conversationId = payload.get("conversationId");
        log.debug("User {} is typing in conversation {}", userId, conversationId);
    }
}
