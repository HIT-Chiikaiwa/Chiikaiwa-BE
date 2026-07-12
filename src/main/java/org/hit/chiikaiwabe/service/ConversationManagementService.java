package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.*;

public interface ConversationManagementService {
    ConversationResponseDto getOrCreateDirectConversation(String userId, CreateDirectConversationRequestDto dto);
    ConversationResponseDto createGroup(String userId, CreateGroupRequestDto dto);
    void addMembers(String userId, String conversationId, AddMembersRequestDto dto);
    void removeMember(String userId, String conversationId, String targetUserId);
    ConversationResponseDto updateGroup(String userId, String conversationId, UpdateGroupRequestDto dto);
    void dissolveGroup(String userId, String conversationId);
    void transferOwnership(String userId, String conversationId, String newOwnerId);
}
