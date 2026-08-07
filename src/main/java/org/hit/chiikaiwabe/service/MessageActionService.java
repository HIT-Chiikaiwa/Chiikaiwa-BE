package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.*;

public interface MessageActionService {
    void sendMessage(String senderId, SendMessageRequestDto dto);
    void markAsRead(String conversationId, String userId);
    void pinMessage(String userId, String messageId);
    void unpinMessage(String userId, String messageId);
    MessageResponseDto replyToMessage(String senderId, String messageId, String content);
    MessageResponseDto forwardMessage(String senderId, String messageId, String targetConversationId);
}
