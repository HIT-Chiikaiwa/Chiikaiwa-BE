package org.hit.chiikaiwabe.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hit.chiikaiwabe.domain.dto.response.MessageResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.ReactionSummaryDto;
import org.hit.chiikaiwabe.repository.ConversationMemberRepository;
import org.hit.chiikaiwabe.service.ChatNotificationService;
import org.hit.chiikaiwabe.service.OnlineStatusService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatNotificationServiceImpl implements ChatNotificationService {

    private final StringRedisTemplate redisTemplate;
    private final OnlineStatusService onlineStatusService;
    private final ConversationMemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    private static final String CHAT_CHANNEL = "chat:broadcast";

    @Async("taskExecutor")
    @Override
    public void broadcastAndNotify(String conversationId, String senderId, MessageResponseDto dto) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "NEW_MESSAGE");
            event.put("conversationId", conversationId);
            event.put("payload", dto);
            redisTemplate.convertAndSend(CHAT_CHANNEL, objectMapper.writeValueAsString(event));

            List<String> memberIds = memberRepository.findActiveUserIds(conversationId);
            for (String memberId : memberIds) {
                if (!memberId.equals(senderId)) {
                    onlineStatusService.incrementUnread(conversationId, memberId);
                }
            }
        } catch (Exception e) {
            log.error("Failed to broadcast message for conversation {}: {}", conversationId, e.getMessage(), e);
        }
    }

    @Async("taskExecutor")
    @Override
    public void broadcastSystemEvent(String conversationId, MessageResponseDto dto) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "SYSTEM_MESSAGE");
            event.put("conversationId", conversationId);
            event.put("payload", dto);
            redisTemplate.convertAndSend(CHAT_CHANNEL, objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("Failed to broadcast system event for conversation {}: {}", conversationId, e.getMessage(), e);
        }
    }

    @Override
    public void broadcastReactionUpdate(String conversationId, String messageId, List<ReactionSummaryDto> reactions) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "REACTION_UPDATE");
            event.put("conversationId", conversationId);

            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "REACTION_UPDATE");
            payload.put("messageId", messageId);
            payload.put("reactions", reactions);
            event.put("payload", payload);

            redisTemplate.convertAndSend(CHAT_CHANNEL, objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("Failed to broadcast reaction update for message {}: {}", messageId, e.getMessage(), e);
        }
    }

    @Async("taskExecutor")
    @Override
    public void broadcastRawEvent(String conversationId, Map<String, Object> payload) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "RAW_EVENT");
            event.put("conversationId", conversationId);
            event.put("payload", payload);
            redisTemplate.convertAndSend(CHAT_CHANNEL, objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("Failed to broadcast raw event for conversation {}: {}", conversationId, e.getMessage(), e);
        }
    }
}
