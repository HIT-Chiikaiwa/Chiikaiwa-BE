package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {
    ConversationResponseDto getOrCreateDirectConversation(String userId, CreateDirectConversationRequestDto dto);
    ConversationResponseDto createGroup(String userId, CreateGroupRequestDto dto);
    void sendMessage(String senderId, SendMessageRequestDto dto);
    Page<ConversationResponseDto> getConversations(String userId, Pageable pageable);
    Page<MessageResponseDto> getMessages(String conversationId, String userId, Pageable pageable);
    void addMembers(String userId, String conversationId, AddMembersRequestDto dto);
    void removeMember(String userId, String conversationId, String targetUserId);
    ConversationResponseDto updateGroup(String userId, String conversationId, UpdateGroupRequestDto dto);
    void markAsRead(String conversationId, String userId);

    Page<ConversationResponseDto> searchConversations(String userId, String keyword, Pageable pageable);
    Page<MessageResponseDto> searchMessages(String conversationId, String userId, String keyword, Pageable pageable);
    CommonResponseDto pinMessage(String userId, String messageId);
    CommonResponseDto unpinMessage(String userId, String messageId);
    List<MessageResponseDto> getPinnedMessages(String conversationId, String userId);
    MessageResponseDto replyToMessage(String senderId, String messageId, String content);
    MessageResponseDto forwardMessage(String senderId, String messageId, String targetConversationId);
    CommonResponseDto dissolveGroup(String userId, String conversationId);
    CommonResponseDto transferOwnership(String userId, String conversationId, String newOwnerId);
}
