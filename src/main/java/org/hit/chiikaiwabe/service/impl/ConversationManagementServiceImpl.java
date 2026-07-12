package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.component.ChatHelper;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.*;
import org.hit.chiikaiwabe.domain.entity.*;
import org.hit.chiikaiwabe.domain.enums.ConversationType;
import org.hit.chiikaiwabe.domain.enums.MemberRole;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.repository.ConversationMemberRepository;
import org.hit.chiikaiwabe.repository.ConversationRepository;
import org.hit.chiikaiwabe.service.ChatNotificationService;
import org.hit.chiikaiwabe.service.ConversationManagementService;
import org.hit.chiikaiwabe.service.UserBlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationManagementServiceImpl implements ConversationManagementService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final UserBlockService userBlockService;
    private final ChatNotificationService chatNotificationService;
    private final ChatHelper chatHelper;

    @Value("${chat.max-group-members:30}")
    private int maxGroupMembers;

    @Override
    @Transactional
    public ConversationResponseDto getOrCreateDirectConversation(String userId, CreateDirectConversationRequestDto dto) {
        if (userId.equals(dto.getTargetUserId())) {
            throw new InvalidException(ErrorMessage.Chat.ERR_SELF_CHAT);
        }

        User currentUser = chatHelper.findUserById(userId);
        User targetUser = chatHelper.findUserById(dto.getTargetUserId());

        if (userBlockService.isBlocked(userId, dto.getTargetUserId())) {
            throw new ForbiddenException(ErrorMessage.Chat.ERR_USER_BLOCKED);
        }

        Optional<Conversation> existingConv = conversationRepository
                .findDirectConversation(userId, dto.getTargetUserId());

        if (existingConv.isPresent()) {
            return chatHelper.toConversationResponseDto(existingConv.get(), userId);
        }

        Conversation conv = Conversation.builder()
                .type(ConversationType.DIRECT)
                .createdBy(currentUser)
                .build();
        conv = conversationRepository.save(conv);

        LocalDateTime now = LocalDateTime.now();
        memberRepository.save(ConversationMember.builder()
                .conversation(conv).user(currentUser)
                .role(MemberRole.MEMBER).joinedAt(now).lastReadAt(now).build());
        memberRepository.save(ConversationMember.builder()
                .conversation(conv).user(targetUser)
                .role(MemberRole.MEMBER).joinedAt(now).lastReadAt(now).build());

        return chatHelper.toConversationResponseDto(conv, userId);
    }

    @Override
    @Transactional
    public ConversationResponseDto createGroup(String userId, CreateGroupRequestDto dto) {
        User owner = chatHelper.findUserById(userId);

        if (dto.getMemberIds().size() + 1 > maxGroupMembers) {
            throw new InvalidException(ErrorMessage.Chat.ERR_GROUP_FULL);
        }

        Conversation conv = Conversation.builder()
                .type(ConversationType.GROUP)
                .groupName(dto.getGroupName())
                .groupAvatar(dto.getGroupAvatar())
                .maxMembers(maxGroupMembers)
                .createdBy(owner)
                .build();
        conv = conversationRepository.save(conv);

        LocalDateTime now = LocalDateTime.now();
        memberRepository.save(ConversationMember.builder()
                .conversation(conv).user(owner)
                .role(MemberRole.OWNER).joinedAt(now).lastReadAt(now).build());

        for (String memberId : dto.getMemberIds()) {
            if (memberId.equals(userId)) continue;
            User member = chatHelper.findUserById(memberId);
            memberRepository.save(ConversationMember.builder()
                    .conversation(conv).user(member)
                    .role(MemberRole.MEMBER).joinedAt(now).lastReadAt(now).build());
        }

        String content = owner.getLastName() + " " + owner.getFirstName() + " đã tạo nhóm";
        chatHelper.createSystemMessage(conv, content);

        return chatHelper.toConversationResponseDto(conv, userId);
    }

    @Override
    @Transactional
    public void addMembers(String userId, String conversationId, AddMembersRequestDto dto) {
        Conversation conversation = chatHelper.findConversationById(conversationId);
        chatHelper.findActiveMember(conversationId, userId);
        User adder = chatHelper.findUserById(userId);

        int currentCount = memberRepository.countActiveMembers(conversationId);
        if (currentCount + dto.getMemberIds().size() > conversation.getMaxMembers()) {
            throw new InvalidException(ErrorMessage.Chat.ERR_GROUP_FULL);
        }

        LocalDateTime now = LocalDateTime.now();
        for (String memberId : dto.getMemberIds()) {
            User newMember = chatHelper.findUserById(memberId);

            Optional<ConversationMember> existingMember =
                    memberRepository.findByConversationIdAndUserId(conversationId, memberId);

            if (existingMember.isPresent()) {
                ConversationMember cm = existingMember.get();
                if (cm.getLeftAt() != null) {
                    cm.setLeftAt(null);
                    cm.setJoinedAt(now);
                    cm.setLastReadAt(now);
                    memberRepository.save(cm);
                }
            } else {
                memberRepository.save(ConversationMember.builder()
                        .conversation(conversation).user(newMember)
                        .role(MemberRole.MEMBER).joinedAt(now).lastReadAt(now).build());
            }

            String content = adder.getLastName() + " " + adder.getFirstName() +
                    " đã thêm " + newMember.getLastName() + " " + newMember.getFirstName();
            Message sysMsg = chatHelper.createSystemMessage(conversation, content);
            chatNotificationService.broadcastSystemEvent(conversationId, chatHelper.toMessageResponseDto(sysMsg));
        }
    }

    @Override
    @Transactional
    public void removeMember(String userId, String conversationId, String targetUserId) {
        Conversation conversation = chatHelper.findConversationById(conversationId);
        ConversationMember remover = chatHelper.findActiveMember(conversationId, userId);
        ConversationMember target = chatHelper.findActiveMember(conversationId, targetUserId);

        boolean isSelf = userId.equals(targetUserId);

        if (!isSelf) {
            if (remover.getRole() == MemberRole.MEMBER) {
                throw new ForbiddenException(ErrorMessage.FORBIDDEN);
            }
        }

        target.setLeftAt(LocalDateTime.now());
        memberRepository.save(target);

        User targetUser = chatHelper.findUserById(targetUserId);
        String content;
        if (isSelf) {
            content = targetUser.getLastName() + " " + targetUser.getFirstName() + " đã rời nhóm";
        } else {
            User removerUser = chatHelper.findUserById(userId);
            content = removerUser.getLastName() + " " + removerUser.getFirstName() +
                    " đã xóa " + targetUser.getLastName() + " " + targetUser.getFirstName() + " khỏi nhóm";
        }
        Message sysMsg = chatHelper.createSystemMessage(conversation, content);
        chatNotificationService.broadcastSystemEvent(conversationId, chatHelper.toMessageResponseDto(sysMsg));
    }

    @Override
    @Transactional
    public ConversationResponseDto updateGroup(String userId, String conversationId, UpdateGroupRequestDto dto) {
        Conversation conversation = chatHelper.findConversationById(conversationId);
        chatHelper.findActiveMember(conversationId, userId);
        User updater = chatHelper.findUserById(userId);

        StringBuilder changes = new StringBuilder();

        if (dto.getGroupName() != null && !dto.getGroupName().isBlank()) {
            String oldName = conversation.getGroupName();
            conversation.setGroupName(dto.getGroupName());
            changes.append(updater.getLastName()).append(" ").append(updater.getFirstName())
                    .append(" đã đổi tên nhóm từ \"").append(oldName).append("\" thành \"")
                    .append(dto.getGroupName()).append("\"");
        }

        if (dto.getGroupAvatar() != null) {
            conversation.setGroupAvatar(dto.getGroupAvatar());
            if (changes.length() == 0) {
                changes.append(updater.getLastName()).append(" ").append(updater.getFirstName())
                        .append(" đã đổi ảnh nhóm");
            }
        }

        conversationRepository.save(conversation);

        if (changes.length() > 0) {
            Message sysMsg = chatHelper.createSystemMessage(conversation, changes.toString());
            chatNotificationService.broadcastSystemEvent(conversationId, chatHelper.toMessageResponseDto(sysMsg));
        }

        return chatHelper.toConversationResponseDto(conversation, userId);
    }

    @Override
    @Transactional
    public void dissolveGroup(String userId, String conversationId) {
        Conversation conversation = chatHelper.findConversationById(conversationId);

        if (conversation.getType() == ConversationType.DIRECT) {
            throw new InvalidException(ErrorMessage.Chat.ERR_CANNOT_DISSOLVE_DIRECT);
        }

        ConversationMember owner = chatHelper.findActiveMember(conversationId, userId);
        if (owner.getRole() != MemberRole.OWNER) {
            throw new ForbiddenException(ErrorMessage.Chat.ERR_NOT_OWNER);
        }

        List<ConversationMember> activeMembers = memberRepository.findActiveMembers(conversationId);
        LocalDateTime now = LocalDateTime.now();
        for (ConversationMember member : activeMembers) {
            member.setLeftAt(now);
            memberRepository.save(member);
        }

        User ownerUser = chatHelper.findUserById(userId);
        String content = ownerUser.getLastName() + " " + ownerUser.getFirstName() + " đã giải tán nhóm";
        Message sysMsg = chatHelper.createSystemMessage(conversation, content);
        chatNotificationService.broadcastSystemEvent(conversationId, chatHelper.toMessageResponseDto(sysMsg));

        log.info("Group {} dissolved by {}", conversationId, userId);
    }

    @Override
    @Transactional
    public void transferOwnership(String userId, String conversationId, String newOwnerId) {
        if (userId.equals(newOwnerId)) {
            throw new InvalidException(ErrorMessage.Chat.ERR_CANNOT_TRANSFER_TO_SELF);
        }

        Conversation conversation = chatHelper.findConversationById(conversationId);

        if (conversation.getType() == ConversationType.DIRECT) {
            throw new InvalidException(ErrorMessage.Chat.ERR_CANNOT_DISSOLVE_DIRECT);
        }

        ConversationMember currentOwner = chatHelper.findActiveMember(conversationId, userId);
        if (currentOwner.getRole() != MemberRole.OWNER) {
            throw new ForbiddenException(ErrorMessage.Chat.ERR_NOT_OWNER);
        }

        ConversationMember newOwner = chatHelper.findActiveMember(conversationId, newOwnerId);

        currentOwner.setRole(MemberRole.MEMBER);
        newOwner.setRole(MemberRole.OWNER);
        memberRepository.save(currentOwner);
        memberRepository.save(newOwner);

        User oldOwnerUser = chatHelper.findUserById(userId);
        User newOwnerUser = chatHelper.findUserById(newOwnerId);
        String content = oldOwnerUser.getLastName() + " " + oldOwnerUser.getFirstName() +
                " đã chuyển quyền trưởng nhóm cho " +
                newOwnerUser.getLastName() + " " + newOwnerUser.getFirstName();
        Message sysMsg = chatHelper.createSystemMessage(conversation, content);
        chatNotificationService.broadcastSystemEvent(conversationId, chatHelper.toMessageResponseDto(sysMsg));

        log.info("Ownership of group {} transferred from {} to {}", conversationId, userId, newOwnerId);
    }
}
