package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.ReactionSummaryDto;
import org.hit.chiikaiwabe.domain.entity.Message;
import org.hit.chiikaiwabe.domain.entity.MessageReaction;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.repository.MessageReactionRepository;
import org.hit.chiikaiwabe.repository.MessageRepository;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.service.MessageReactionService;
import org.hit.chiikaiwabe.service.ChatNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageReactionServiceImpl implements MessageReactionService {

    private final MessageReactionRepository reactionRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatNotificationService chatNotificationService;

    @Override
    @Transactional
    public void addReaction(String userId, String messageId, String emoji) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_MESSAGE_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId}));

        Optional<MessageReaction> existing = reactionRepository.findByMessageIdAndUserId(messageId, userId);
        if (existing.isPresent()) {
            MessageReaction reaction = existing.get();
            reaction.setEmoji(emoji);
            reactionRepository.save(reaction);
        } else {
            MessageReaction reaction = MessageReaction.builder()
                    .message(message)
                    .user(user)
                    .emoji(emoji)
                    .build();
            reactionRepository.save(reaction);
        }

        broadcastReactionUpdate(message.getConversation().getId(), messageId);
    }

    @Override
    @Transactional
    public void removeReaction(String userId, String messageId) {
        reactionRepository.deleteByMessageIdAndUserId(messageId, userId);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_MESSAGE_NOT_FOUND));
        broadcastReactionUpdate(message.getConversation().getId(), messageId);
    }

    @Override
    public List<ReactionSummaryDto> getReactions(String messageId) {
        List<MessageReaction> reactions = reactionRepository.findByMessageId(messageId);

        Map<String, List<MessageReaction>> grouped = reactions.stream()
                .collect(Collectors.groupingBy(MessageReaction::getEmoji));

        return grouped.entrySet().stream()
                .map(entry -> ReactionSummaryDto.builder()
                        .emoji(entry.getKey())
                        .count(entry.getValue().size())
                        .userIds(entry.getValue().stream()
                                .map(r -> r.getUser().getId())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    private void broadcastReactionUpdate(String conversationId, String messageId) {
        List<ReactionSummaryDto> reactions = getReactions(messageId);
        chatNotificationService.broadcastReactionUpdate(conversationId, messageId, reactions);
    }
}
