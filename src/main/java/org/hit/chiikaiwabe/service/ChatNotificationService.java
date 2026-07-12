package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.response.MessageResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.ReactionSummaryDto;

import java.util.List;
import java.util.Map;


public interface ChatNotificationService {


    void broadcastAndNotify(String conversationId, String senderId, MessageResponseDto dto);


    void broadcastSystemEvent(String conversationId, MessageResponseDto dto);


    void broadcastReactionUpdate(String conversationId, String messageId, List<ReactionSummaryDto> reactions);


    void broadcastRawEvent(String conversationId, Map<String, Object> payload);
}
