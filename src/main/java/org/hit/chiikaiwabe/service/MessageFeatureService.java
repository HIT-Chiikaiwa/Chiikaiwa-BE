package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.request.ScheduleInviteRequestDto;
import org.hit.chiikaiwabe.domain.entity.Conversation;
import org.hit.chiikaiwabe.domain.entity.Message;
import org.hit.chiikaiwabe.domain.entity.MessageDeletion;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.MessageType;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.repository.ConversationRepository;
import org.hit.chiikaiwabe.repository.MessageDeletionRepository;
import org.hit.chiikaiwabe.repository.MessageRepository;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
public class MessageFeatureService {

    private final MessageRepository messageRepository;
    private final MessageDeletionRepository messageDeletionRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageFeatureService(MessageRepository messageRepository,
                                 MessageDeletionRepository messageDeletionRepository,
                                 UserRepository userRepository,
                                 ConversationRepository conversationRepository,
                                 SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.messageDeletionRepository = messageDeletionRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void validateConversationAccess(String userId, String conversationId) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new NotFoundException(ErrorMessage.Chat.ERR_CONVERSATION_NOT_FOUND);
        }
    }

    public void deleteMessageForMe(String userId, String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_MESSAGE_NOT_FOUND));
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
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_MESSAGE_NOT_FOUND));

        if (!message.getSender().getId().equals(userId)) {
            throw new ForbiddenException(ErrorMessage.Chat.ERR_NOT_AUTHOR);
        }

        long minutesBetween = ChronoUnit.MINUTES.between(message.getCreatedDate(), LocalDateTime.now());
        if (minutesBetween > 30) {
            throw new ForbiddenException(ErrorMessage.Chat.ERR_CANNOT_RECALL_AFTER_30_MINS);
        }

        message.setIsRecalled(true);
        messageRepository.save(message);

        // Broadcast to WebSocket topic/queue
        String destination = "/topic/conversation." + message.getConversation().getId();
        messagingTemplate.convertAndSend(destination, message);
    }

    public Message createScheduleInvite(String userId, String conversationId, ScheduleInviteRequestDto requestDto) {
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
        messagingTemplate.convertAndSend(destination, message);
        
        return message;
    }
}
