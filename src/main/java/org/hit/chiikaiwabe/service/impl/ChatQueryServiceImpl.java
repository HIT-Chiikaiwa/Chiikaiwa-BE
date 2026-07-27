package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.component.ChatHelper;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.domain.dto.response.ConversationResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.MessageResponseDto;
import org.hit.chiikaiwabe.domain.entity.Conversation;
import org.hit.chiikaiwabe.domain.entity.ConversationMember;
import org.hit.chiikaiwabe.domain.entity.Message;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.repository.ConversationMemberRepository;
import org.hit.chiikaiwabe.repository.ConversationRepository;
import org.hit.chiikaiwabe.repository.MessageRepository;
import org.hit.chiikaiwabe.service.ChatQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatQueryServiceImpl implements ChatQueryService {

    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final ChatHelper chatHelper;

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

        Map<String, Integer> memberCountMap = memberRepository
                .countActiveMembersByConversationIds(conversationIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

        Map<String, ConversationMember> membershipMap = memberRepository
                .findByUserIdAndConversationIdIn(userId, conversationIds)
                .stream()
                .collect(Collectors.toMap(m -> m.getConversation().getId(), Function.identity()));

        List<ConversationResponseDto> result = conversationPage.getContent().stream()
                .map(conv -> {
                    Conversation fullConv = fetchedMap.getOrDefault(conv.getId(), conv);
                    return chatHelper.toConversationResponseDto(fullConv, userId, memberCountMap, membershipMap);
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

        return messages.map(chatHelper::toMessageResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationResponseDto> searchConversations(String userId, String keyword, Pageable pageable) {
        String searchKeyword = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        Page<Conversation> conversations = conversationRepository.searchByKeyword(userId, searchKeyword, pageable);

        List<String> conversationIds = conversations.getContent().stream()
                .map(Conversation::getId).collect(Collectors.toList());

        if (conversationIds.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        Map<String, Integer> memberCountMap = memberRepository
                .countActiveMembersByConversationIds(conversationIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

        Map<String, ConversationMember> membershipMap = memberRepository
                .findByUserIdAndConversationIdIn(userId, conversationIds)
                .stream()
                .collect(Collectors.toMap(m -> m.getConversation().getId(), Function.identity()));

        List<ConversationResponseDto> result = conversations.getContent().stream()
                .map(conv -> chatHelper.toConversationResponseDto(conv, userId, memberCountMap, membershipMap))
                .collect(Collectors.toList());

        return new PageImpl<>(result, pageable, conversations.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDto> searchMessages(String conversationId, String userId, String keyword, Pageable pageable) {
        chatHelper.findActiveMember(conversationId, userId);
        String searchKeyword = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        Page<Message> messages = messageRepository.searchMessages(conversationId, userId, searchKeyword, pageable);
        return messages.map(chatHelper::toMessageResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponseDto> getPinnedMessages(String conversationId, String userId) {
        chatHelper.findActiveMember(conversationId, userId);
        List<Message> pinned = messageRepository.findPinnedMessages(conversationId);
        return pinned.stream().map(chatHelper::toMessageResponseDto).collect(Collectors.toList());
    }
}
