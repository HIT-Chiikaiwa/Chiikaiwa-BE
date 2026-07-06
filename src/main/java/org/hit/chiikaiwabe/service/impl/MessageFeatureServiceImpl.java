package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.domain.dto.request.ScheduleInviteRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.MessageResponseDto;
import org.hit.chiikaiwabe.domain.mapper.MessageMapper;
import org.hit.chiikaiwabe.domain.entity.Conversation;
import org.hit.chiikaiwabe.domain.entity.Message;
import org.hit.chiikaiwabe.domain.entity.MessageDeletion;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.MessageType;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.repository.ConversationMemberRepository;
import org.hit.chiikaiwabe.repository.ConversationRepository;
import org.hit.chiikaiwabe.repository.MessageDeletionRepository;
import org.hit.chiikaiwabe.repository.MessageRepository;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.service.MessageFeatureService;
import org.hit.chiikaiwabe.service.UserBlockService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
public class MessageFeatureServiceImpl implements MessageFeatureService {

    private final MessageRepository messageRepository;
    private final MessageDeletionRepository messageDeletionRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageMapper messageMapper;
    private final UserBlockService userBlockService;

    public MessageFeatureServiceImpl(MessageRepository messageRepository,
                                 MessageDeletionRepository messageDeletionRepository,
                                 UserRepository userRepository,
                                 ConversationRepository conversationRepository,
                                 ConversationMemberRepository conversationMemberRepository,
                                 SimpMessagingTemplate messagingTemplate,
                                 MessageMapper messageMapper,
                                 UserBlockService userBlockService) {
        this.messageRepository = messageRepository;
        this.messageDeletionRepository = messageDeletionRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.messagingTemplate = messagingTemplate;
        this.messageMapper = messageMapper;
        this.userBlockService = userBlockService;
    }

    public void validateConversationAccess(String userId, String conversationId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new NotFoundException(ErrorMessage.Chat.ERR_CONVERSATION_NOT_FOUND);
        }
        conversationMemberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ForbiddenException(ErrorMessage.Chat.ERR_NOT_IN_CONVERSATION));
    }

    public void validateNotBlockedInDirectConversation(String userId, String conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_CONVERSATION_NOT_FOUND));

        if (conversation.getType() == org.hit.chiikaiwabe.domain.enums.ConversationType.DIRECT) {
            java.util.List<org.hit.chiikaiwabe.domain.entity.ConversationMember> members = conversationMemberRepository.findByConversationId(conversationId);
            for (org.hit.chiikaiwabe.domain.entity.ConversationMember member : members) {
                if (!member.getUser().getId().equals(userId)) {
                    if (userBlockService.isBlocked(userId, member.getUser().getId())) {
                        throw new ForbiddenException(ErrorMessage.Chat.ERR_BLOCKED);
                    }
                }
            }
        }
    }

    public void deleteMessageForMe(String userId, String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_MESSAGE_NOT_FOUND));

        validateConversationAccess(userId, message.getConversation().getId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));

        if (messageDeletionRepository.findByMessageIdAndUserId(messageId, userId).isEmpty()) {
            MessageDeletion deletion = MessageDeletion.builder()
                    .message(message)
                    .user(user)
                    .build();
            messageDeletionRepository.save(deletion);
        }
    }

    public void recallMessage(String userId, String messageId) {
        Message message = messageRepository.findByIdWithDetails(messageId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_MESSAGE_NOT_FOUND));

        validateConversationAccess(userId, message.getConversation().getId());

        if (message.getIsRecalled() != null && message.getIsRecalled()) {
            throw new InvalidException(ErrorMessage.Chat.ERR_MESSAGE_ALREADY_RECALLED);
        }

        if (!message.getSender().getId().equals(userId)) {
            throw new ForbiddenException(ErrorMessage.Chat.ERR_NOT_AUTHOR);
        }

        long minutesBetween = ChronoUnit.MINUTES.between(message.getCreatedDate(), LocalDateTime.now());
        if (minutesBetween > 30) {
            throw new ForbiddenException(ErrorMessage.Chat.ERR_CANNOT_RECALL_AFTER_30_MINS);
        }

        message.setIsRecalled(true);
        messageRepository.save(message);

        String destination = "/topic/conversation." + message.getConversation().getId();
        MessageResponseDto messageDto = messageMapper.toDto(message);
        
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messagingTemplate.convertAndSend(destination, messageDto);
            }
        });
    }

    public MessageResponseDto createScheduleInvite(String userId, String conversationId, ScheduleInviteRequestDto requestDto) {
        validateConversationAccess(userId, conversationId);
        validateNotBlockedInDirectConversation(userId, conversationId);

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_CONVERSATION_NOT_FOUND));

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(requestDto.getPayload())
                .messageType(MessageType.SCHEDULE_INVITE)
                .build();

        message = messageRepository.save(message);

        conversation.setLastMessage(message);
        conversationRepository.save(conversation);

        String destination = "/topic/conversation." + conversationId;
        MessageResponseDto messageDto = messageMapper.toDto(message);
        
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messagingTemplate.convertAndSend(destination, messageDto);
            }
        });
        
        return messageDto;
    }
}
