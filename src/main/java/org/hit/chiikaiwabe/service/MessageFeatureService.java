package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.request.ScheduleInviteRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.MessageResponseDto;

public interface MessageFeatureService {
    void validateConversationAccess(String userId, String conversationId);
    void validateNotBlockedInDirectConversation(String userId, String conversationId);
    void deleteMessageForMe(String userId, String messageId);
    void recallMessage(String userId, String messageId);
    MessageResponseDto createScheduleInvite(String userId, String conversationId, ScheduleInviteRequestDto requestDto);
}
