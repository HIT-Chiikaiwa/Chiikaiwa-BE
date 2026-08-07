package org.hit.chiikaiwabe.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody());
            JsonNode event = objectMapper.readTree(json);

            String type = event.get("type").asText();
            String conversationId = event.get("conversationId").asText();
            JsonNode payload = event.get("payload");

            String destination = "/topic/conversation." + conversationId;

            switch (type) {
                case "NEW_MESSAGE", "SYSTEM_MESSAGE" -> {
                    Object messageDto = objectMapper.treeToValue(payload, Object.class);
                    messagingTemplate.convertAndSend(destination, messageDto);
                }
                case "REACTION_UPDATE", "RAW_EVENT" -> {
                    Object rawPayload = objectMapper.treeToValue(payload, Object.class);
                    messagingTemplate.convertAndSend(destination, rawPayload);
                }
                default -> log.warn("Unknown chat event type: {}", type);
            }

            log.debug("Broadcast {} to {}", type, destination);
        } catch (Exception e) {
            log.error("Error processing Redis chat message: {}", e.getMessage(), e);
        }
    }
}
