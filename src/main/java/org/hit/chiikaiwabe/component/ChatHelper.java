package org.hit.chiikaiwabe.component;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.domain.dto.response.ConversationResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.MessageResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.ReplyMessageDto;
import org.hit.chiikaiwabe.domain.entity.Conversation;
import org.hit.chiikaiwabe.domain.entity.ConversationMember;
import org.hit.chiikaiwabe.domain.entity.Message;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.ConversationType;
import org.hit.chiikaiwabe.domain.enums.MessageType;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.repository.ConversationMemberRepository;
import org.hit.chiikaiwabe.repository.ConversationRepository;
import org.hit.chiikaiwabe.repository.MessageRepository;
import org.hit.chiikaiwabe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatHelper {
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageRepository messageRepository;

    public User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId}));
    }

    public Conversation findConversationById(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_CONVERSATION_NOT_FOUND));
    }

    public ConversationMember findActiveMember(String conversationId, String userId) {
        ConversationMember member = memberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ForbiddenException(ErrorMessage.Chat.ERR_NOT_MEMBER));
        if (member.getLeftAt() != null) {
            throw new ForbiddenException(ErrorMessage.Chat.ERR_ALREADY_LEFT);
        }
        return member;
    }

    public Message createSystemMessage(Conversation conversation, String content) {
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

    public MessageResponseDto toMessageResponseDto(Message msg) {
        ReplyMessageDto replyDto = null;
        if (msg.getReplyToMessage() != null) {
            Message replyMsg = msg.getReplyToMessage();
            replyDto = ReplyMessageDto.builder()
                    .id(replyMsg.getId())
                    .senderName(replyMsg.getSender() != null ?
                            replyMsg.getSender().getLastName() + " " + replyMsg.getSender().getFirstName() : "System")
                    .content(replyMsg.getIsRecalled() ? null : replyMsg.getContent())
                    .messageType(replyMsg.getMessageType().name())
                    .build();
        }

        ReplyMessageDto forwardDto = null;
        if (msg.getForwardedFromMessage() != null) {
            Message fwdMsg = msg.getForwardedFromMessage();
            forwardDto = ReplyMessageDto.builder()
                    .id(fwdMsg.getId())
                    .senderName(fwdMsg.getSender() != null ?
                            fwdMsg.getSender().getLastName() + " " + fwdMsg.getSender().getFirstName() : "System")
                    .content(fwdMsg.getIsRecalled() ? null : fwdMsg.getContent())
                    .messageType(fwdMsg.getMessageType().name())
                    .build();
        }

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
                .replyToMessage(replyDto)
                .forwardedFrom(forwardDto)
                .isPinned(msg.getIsPinned())
                .reactions(new ArrayList<>())
                .build();
    }

    public ConversationResponseDto toConversationResponseDto(Conversation conv, String userId,
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

    public ConversationResponseDto toConversationResponseDto(Conversation conv, String userId) {
        int memberCount = memberRepository.countActiveMembers(conv.getId());
        Map<String, Integer> countMap = Map.of(conv.getId(), memberCount);

        ConversationMember membership = memberRepository
                .findByConversationIdAndUserId(conv.getId(), userId).orElse(null);
        Map<String, ConversationMember> memberMap = membership != null
                ? Map.of(conv.getId(), membership) : Map.of();

        return toConversationResponseDto(conv, userId, countMap, memberMap);
    }
}
