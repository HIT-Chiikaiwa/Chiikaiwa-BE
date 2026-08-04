package org.hit.chiikaiwabe.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody());
            JsonNode event = objectMapper.readTree(json);

            String recipientUserId = event.get("recipientUserId").asText();
            JsonNode payloadNode = event.get("payload");
            Map<String, Object> payload = objectMapper.treeToValue(payloadNode, Map.class);

            messagingTemplate.convertAndSendToUser(
                    recipientUserId,
                    "/queue/notifications",
                    payload
            );

            log.debug("Sent notification to user {}: {}", recipientUserId, payload.get("type"));
        } catch (Exception e) {
            log.error("Error processing Redis notification message: {}", e.getMessage(), e);
        }
    }
}
