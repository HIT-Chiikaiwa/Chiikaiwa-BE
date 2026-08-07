package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatQueryService {
    Page<ConversationResponseDto> getConversations(String userId, Pageable pageable);
    Page<MessageResponseDto> getMessages(String conversationId, String userId, Pageable pageable);
    Page<ConversationResponseDto> searchConversations(String userId, String keyword, Pageable pageable);
    Page<MessageResponseDto> searchMessages(String conversationId, String userId, String keyword, Pageable pageable);
    List<MessageResponseDto> getPinnedMessages(String conversationId, String userId);
}
