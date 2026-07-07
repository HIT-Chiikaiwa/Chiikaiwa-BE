package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.*;
import org.hit.chiikaiwabe.domain.entity.*;
import org.hit.chiikaiwabe.domain.enums.ConversationType;
import org.hit.chiikaiwabe.domain.enums.MemberRole;
import org.hit.chiikaiwabe.domain.enums.MessageType;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.repository.*;
import org.hit.chiikaiwabe.service.BlockReportService;
import org.hit.chiikaiwabe.service.ChatService;
import org.hit.chiikaiwabe.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final BlockReportService blockReportService;
    private final SimpMessagingTemplate messagingTemplate;
    private final OnlineStatusService onlineStatusService;

    @Value("${chat.max-group-members:30}")
    private int maxGroupMembers;


    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId}));
    }

    private Conversation findConversationById(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_CONVERSATION_NOT_FOUND));
    }

    private ConversationMember findActiveMember(String conversationId, String userId) {
        ConversationMember member = memberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ForbiddenException(ErrorMessage.Chat.ERR_NOT_MEMBER));
        if (member.getLeftAt() != null) {
            throw new ForbiddenException(ErrorMessage.Chat.ERR_ALREADY_LEFT);
        }
        return member;
    }

    private Message createSystemMessage(Conversation conversation, String content) {
        Message systemMsg = Message.builder()
                .conversation(conversation)
                .sender(null)
                .content(content)
                .messageType(MessageType.SYSTEM)
                .build();
        systemMsg = messageRepository.save(systemMsg);
        conversation.setLastMessage(systemMsg);
        conversationRepository.save(conversation);
        return systemMsg;
    }

    private MessageResponseDto toMessageResponseDto(Message msg) {
        return MessageResponseDto.builder()
                .id(msg.getId())
                .conversationId(msg.getConversation().getId())
                .senderId(msg.getSender() != null ? msg.getSender().getId() : null)
                .senderName(msg.getSender() != null ?
                        msg.getSender().getLastName() + " " + msg.getSender().getFirstName() : "System")
                .senderAvatar(msg.getSender() != null ? msg.getSender().getAvatar() : null)
                .content(msg.getIsRecalled() ? null : msg.getContent())
                .messageType(msg.getMessageType().name())
                .isRecalled(msg.getIsRecalled())
                .createdDate(msg.getCreatedDate())
                .attachments(new ArrayList<>())
                .build();
    }

    private ConversationResponseDto toConversationResponseDto(Conversation conv, String userId,
                                                              Map<String, Integer> memberCountMap, Map<String, ConversationMember> membershipMap) {
        int memberCount = memberCountMap.getOrDefault(conv.getId(), 0);

        int unreadCount = 0;
        ConversationMember membership = membershipMap.get(conv.getId());
        if (membership != null && membership.getLastReadAt() != null) {
            unreadCount = messageRepository.countUnreadMessages(conv.getId(),
                    membership.getLastReadAt(), userId);
        }

        MessageResponseDto lastMsg = null;
        if (conv.getLastMessage() != null) {
            lastMsg = toMessageResponseDto(conv.getLastMessage());
        }

        String displayName = conv.getGroupName();
        String displayAvatar = conv.getGroupAvatar();
        if (conv.getType() == ConversationType.DIRECT) {
            List<ConversationMember> members = memberRepository.findActiveMembers(conv.getId());
            for (ConversationMember m : members) {
                if (!m.getUser().getId().equals(userId)) {
                    displayName = m.getUser().getLastName() + " " + m.getUser().getFirstName();
                    displayAvatar = m.getUser().getAvatar();
                    break;
                }
            }
        }

        boolean hasLeft = membership != null && membership.getLeftAt() != null;

        return ConversationResponseDto.builder()
                .id(conv.getId())
                .type(conv.getType().name())
                .groupName(displayName)
                .groupAvatar(displayAvatar)
                .memberCount(memberCount)
                .lastMessage(lastMsg)
                .unreadCount(unreadCount)
                .hasLeft(hasLeft)
                .build();
    }

    private ConversationResponseDto toConversationResponseDto(Conversation conv, String userId) {
        int memberCount = memberRepository.countActiveMembers(conv.getId());
        Map<String, Integer> countMap = Map.of(conv.getId(), memberCount);

        ConversationMember membership = memberRepository
                .findByConversationIdAndUserId(conv.getId(), userId).orElse(null);
        Map<String, ConversationMember> memberMap = membership != null
                ? Map.of(conv.getId(), membership) : Map.of();

        return toConversationResponseDto(conv, userId, countMap, memberMap);
    }

    private void broadcastToConversation(String conversationId, MessageResponseDto messageDto) {
        messagingTemplate.convertAndSend(
                "/topic/conversation." + conversationId, messageDto);
    }


    @Override
    @Transactional
    public ConversationResponseDto getOrCreateDirectConversation(String userId, CreateDirectConversationRequestDto dto) {
        if (userId.equals(dto.getTargetUserId())) {
            throw new InvalidException(ErrorMessage.Chat.ERR_SELF_CHAT);
        }

        User currentUser = findUserById(userId);
        User targetUser = findUserById(dto.getTargetUserId());

        if (blockReportService.isBlocked(userId, dto.getTargetUserId())) {
            throw new ForbiddenException(ErrorMessage.Chat.ERR_USER_BLOCKED);
        }

        Optional<Conversation> existingConv = conversationRepository
                .findDirectConversation(userId, dto.getTargetUserId());

        if (existingConv.isPresent()) {
            return toConversationResponseDto(existingConv.get(), userId);
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

        return toConversationResponseDto(conv, userId);
    }


    @Override
    @Transactional
    public ConversationResponseDto createGroup(String userId, CreateGroupRequestDto dto) {
        User owner = findUserById(userId);

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
            User member = findUserById(memberId);
            memberRepository.save(ConversationMember.builder()
                    .conversation(conv).user(member)
                    .role(MemberRole.MEMBER).joinedAt(now).lastReadAt(now).build());
        }

        String content = owner.getLastName() + " " + owner.getFirstName() + " đã tạo nhóm";
        createSystemMessage(conv, content);

        return toConversationResponseDto(conv, userId);
    }


    @Override
    @Transactional
    public void sendMessage(String senderId, SendMessageRequestDto dto) {
        User sender = findUserById(senderId);
        Conversation conversation = findConversationById(dto.getConversationId());

        findActiveMember(dto.getConversationId(), senderId);

        if (conversation.getType() == ConversationType.DIRECT) {
            List<String> memberIds = memberRepository.findActiveUserIds(dto.getConversationId());
            for (String memberId : memberIds) {
                if (!memberId.equals(senderId) && blockReportService.isBlocked(senderId, memberId)) {
                    throw new ForbiddenException(ErrorMessage.Chat.ERR_USER_BLOCKED);
                }
            }
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(dto.getContent())
                .messageType(MessageType.TEXT)
                .build();
        message = messageRepository.save(message);

        conversation.setLastMessage(message);
        conversationRepository.save(conversation);

        MessageResponseDto responseDto = toMessageResponseDto(message);
        broadcastToConversation(dto.getConversationId(), responseDto);

        // Increment unread count for other active members
        List<String> activeMemberIds = memberRepository.findActiveUserIds(dto.getConversationId());
        for (String activeMemberId : activeMemberIds) {
            if (!activeMemberId.equals(senderId)) {
                onlineStatusService.incrementUnread(dto.getConversationId(), activeMemberId);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponseDto> getConversations(String userId, Pageable pageable) {
        Page<Conversation> conversationPage = conversationRepository.findAllByUserId(userId, pageable);
        List<String> conversationIds = conversationPage.getContent().stream()
                .map(Conversation::getId).collect(Collectors.toList());

        if (conversationIds.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        List<Conversation> fetched = conversationRepository.findAllWithLastMessageByIds(conversationIds);
        Map<String, Conversation> fetchedMap = fetched.stream()
                .collect(Collectors.toMap(Conversation::getId, Function.identity()));

        Map<String, Integer> memberCountMap = conversationIds.stream()
                .collect(Collectors.toMap(id -> id, id -> memberRepository.countActiveMembers(id)));

        Map<String, ConversationMember> membershipMap = conversationIds.stream()
                .map(id -> memberRepository.findByConversationIdAndUserId(id, userId).orElse(null))
                .filter(m -> m != null)
                .collect(Collectors.toMap(m -> m.getConversation().getId(), Function.identity()));

        List<ConversationResponseDto> result = conversationPage.getContent().stream()
                .map(conv -> {
                    Conversation fullConv = fetchedMap.getOrDefault(conv.getId(), conv);
                    return toConversationResponseDto(fullConv, userId, memberCountMap, membershipMap);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(result, pageable, conversationPage.getTotalElements());
    }


    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDto> getMessages(String conversationId, String userId, Pageable pageable) {
        ConversationMember member = memberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ForbiddenException(ErrorMessage.Chat.ERR_NOT_MEMBER));

        LocalDateTime leftAt = member.getLeftAt();

        Page<Message> messages = messageRepository
                .findByConversationIdForUser(conversationId, userId, leftAt, pageable);

        return messages.map(this::toMessageResponseDto);
    }

    @Override
    @Transactional
    public void addMembers(String userId, String conversationId, AddMembersRequestDto dto) {
        Conversation conversation = findConversationById(conversationId);
        findActiveMember(conversationId, userId);
        User adder = findUserById(userId);

        int currentCount = memberRepository.countActiveMembers(conversationId);
        if (currentCount + dto.getMemberIds().size() > conversation.getMaxMembers()) {
            throw new InvalidException(ErrorMessage.Chat.ERR_GROUP_FULL);
        }

        LocalDateTime now = LocalDateTime.now();
        for (String memberId : dto.getMemberIds()) {
            User newMember = findUserById(memberId);

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
            Message sysMsg = createSystemMessage(conversation, content);
            broadcastToConversation(conversationId, toMessageResponseDto(sysMsg));
        }
    }


    @Override
    @Transactional
    public void removeMember(String userId, String conversationId, String targetUserId) {
        Conversation conversation = findConversationById(conversationId);
        ConversationMember remover = findActiveMember(conversationId, userId);
        ConversationMember target = findActiveMember(conversationId, targetUserId);

        boolean isSelf = userId.equals(targetUserId);

        if (!isSelf) {
            if (remover.getRole() == MemberRole.MEMBER) {
                throw new ForbiddenException(ErrorMessage.FORBIDDEN);
            }
        }

        target.setLeftAt(LocalDateTime.now());
        memberRepository.save(target);

        User targetUser = findUserById(targetUserId);
        String content;
        if (isSelf) {
            content = targetUser.getLastName() + " " + targetUser.getFirstName() + " đã rời nhóm";
        } else {
            User removerUser = findUserById(userId);
            content = removerUser.getLastName() + " " + removerUser.getFirstName() +
                    " đã xóa " + targetUser.getLastName() + " " + targetUser.getFirstName() + " khỏi nhóm";
        }
        Message sysMsg = createSystemMessage(conversation, content);
        broadcastToConversation(conversationId, toMessageResponseDto(sysMsg));
    }

    @Override
    @Transactional
    public ConversationResponseDto updateGroup(String userId, String conversationId, UpdateGroupRequestDto dto) {
        Conversation conversation = findConversationById(conversationId);
        findActiveMember(conversationId, userId);
        User updater = findUserById(userId);

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
            Message sysMsg = createSystemMessage(conversation, changes.toString());
            broadcastToConversation(conversationId, toMessageResponseDto(sysMsg));
        }

        return toConversationResponseDto(conversation, userId);
    }


    @Override
    @Transactional
    public void markAsRead(String conversationId, String userId) {
        ConversationMember member = findActiveMember(conversationId, userId);
        member.setLastReadAt(LocalDateTime.now());
        memberRepository.save(member);

        onlineStatusService.resetUnread(conversationId, userId);
    }
}
