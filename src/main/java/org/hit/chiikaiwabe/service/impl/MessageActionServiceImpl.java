package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.component.ChatHelper;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.*;
import org.hit.chiikaiwabe.domain.entity.*;
import org.hit.chiikaiwabe.domain.enums.ConversationType;
import org.hit.chiikaiwabe.domain.enums.MessageType;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.repository.ConversationMemberRepository;
import org.hit.chiikaiwabe.repository.ConversationRepository;
import org.hit.chiikaiwabe.repository.MessageRepository;
import org.hit.chiikaiwabe.service.ChatNotificationService;
import org.hit.chiikaiwabe.service.MessageActionService;
import org.hit.chiikaiwabe.service.OnlineStatusService;
import org.hit.chiikaiwabe.service.UserBlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageActionServiceImpl implements MessageActionService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final UserBlockService userBlockService;
    private final ChatNotificationService chatNotificationService;
    private final OnlineStatusService onlineStatusService;
    private final ChatHelper chatHelper;

    @Override
    @Transactional
    public void sendMessage(String senderId, SendMessageRequestDto dto) {
        User sender = chatHelper.findUserById(senderId);
        Conversation conversation = chatHelper.findConversationById(dto.getConversationId());

        chatHelper.findActiveMember(dto.getConversationId(), senderId);

        if (conversation.getType() == ConversationType.DIRECT) {
            List<String> memberIds = memberRepository.findActiveUserIds(dto.getConversationId());
            for (String memberId : memberIds) {
                if (!memberId.equals(senderId) && userBlockService.isBlocked(senderId, memberId)) {
                    throw new ForbiddenException(ErrorMessage.Chat.ERR_USER_BLOCKED);
                }
            }
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(dto.getContent())
                .messageType(dto.getMessageType() != null ? dto.getMessageType() : MessageType.TEXT)
                .build();
        message = messageRepository.save(message);

        conversation.setLastMessage(message);
        conversationRepository.save(conversation);

        MessageResponseDto responseDto = chatHelper.toMessageResponseDto(message);
        chatNotificationService.broadcastAndNotify(dto.getConversationId(), senderId, responseDto);
    }

    @Override
    @Transactional
    public void markAsRead(String conversationId, String userId) {
        ConversationMember member = chatHelper.findActiveMember(conversationId, userId);
        member.setLastReadAt(LocalDateTime.now());
        memberRepository.save(member);

        onlineStatusService.resetUnread(conversationId, userId);
    }

    @Override
    @Transactional
    public void pinMessage(String userId, String messageId) {
        Message message = messageRepository.findByIdWithDetails(messageId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_MESSAGE_NOT_FOUND));
        chatHelper.findActiveMember(message.getConversation().getId(), userId);

        if (Boolean.TRUE.equals(message.getIsPinned())) {
            throw new InvalidException(ErrorMessage.Chat.ERR_ALREADY_PINNED);
        }

        message.setIsPinned(true);
        messageRepository.save(message);

        User pinner = chatHelper.findUserById(userId);
        String content = pinner.getLastName() + " " + pinner.getFirstName() + " đã ghim một tin nhắn";
        Message sysMsg = chatHelper.createSystemMessage(message.getConversation(), content);
        chatNotificationService.broadcastSystemEvent(message.getConversation().getId(), chatHelper.toMessageResponseDto(sysMsg));
    }

    @Override
    @Transactional
    public void unpinMessage(String userId, String messageId) {
        Message message = messageRepository.findByIdWithDetails(messageId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_MESSAGE_NOT_FOUND));
        chatHelper.findActiveMember(message.getConversation().getId(), userId);

        if (!Boolean.TRUE.equals(message.getIsPinned())) {
            throw new InvalidException(ErrorMessage.Chat.ERR_NOT_PINNED);
        }

        message.setIsPinned(false);
        messageRepository.save(message);
    }

    @Override
    @Transactional
    public MessageResponseDto replyToMessage(String senderId, String messageId, String content) {
        Message originalMessage = messageRepository.findByIdWithDetails(messageId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_MESSAGE_NOT_FOUND));

        if (Boolean.TRUE.equals(originalMessage.getIsRecalled())) {
            throw new InvalidException(ErrorMessage.Chat.ERR_CANNOT_REPLY_RECALLED);
        }

        String conversationId = originalMessage.getConversation().getId();
        User sender = chatHelper.findUserById(senderId);
        chatHelper.findActiveMember(conversationId, senderId);

        Conversation conversation = originalMessage.getConversation();

        Message replyMessage = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .messageType(MessageType.TEXT)
                .replyToMessage(originalMessage)
                .build();
        replyMessage = messageRepository.save(replyMessage);

        conversation.setLastMessage(replyMessage);
        conversationRepository.save(conversation);

        MessageResponseDto responseDto = chatHelper.toMessageResponseDto(replyMessage);
        chatNotificationService.broadcastAndNotify(conversationId, senderId, responseDto);

        return responseDto;
    }

    @Override
    @Transactional
    public MessageResponseDto forwardMessage(String senderId, String messageId, String targetConversationId) {
        Message originalMessage = messageRepository.findByIdWithDetails(messageId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_MESSAGE_NOT_FOUND));

        if (Boolean.TRUE.equals(originalMessage.getIsRecalled())) {
            throw new InvalidException(ErrorMessage.Chat.ERR_CANNOT_FORWARD_RECALLED);
        }

        Conversation targetConversation = conversationRepository.findById(targetConversationId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_TARGET_CONVERSATION_NOT_FOUND));
        User sender = chatHelper.findUserById(senderId);
        chatHelper.findActiveMember(targetConversationId, senderId);

        Message forwardedMessage = Message.builder()
                .conversation(targetConversation)
                .sender(sender)
                .content(originalMessage.getContent())
                .messageType(originalMessage.getMessageType())
                .forwardedFromMessage(originalMessage)
                .build();
        forwardedMessage = messageRepository.save(forwardedMessage);

        targetConversation.setLastMessage(forwardedMessage);
        conversationRepository.save(targetConversation);

        MessageResponseDto responseDto = chatHelper.toMessageResponseDto(forwardedMessage);
        chatNotificationService.broadcastAndNotify(targetConversationId, senderId, responseDto);

        return responseDto;
    }
}
