package org.hit.chiikaiwabe.security;

import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.repository.ConversationMemberRepository;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WebSocketAuthorizationService {
    private static final String CONVERSATION_TOPIC = "/topic/conversation.";
    private static final Set<String> USER_QUEUES = Set.of("/user/queue/notifications", "/user/queue/friendship");
    private static final Set<String> SEND_DESTINATIONS = Set.of("/app/chat.send", "/app/chat.read", "/app/chat.typing");
    private final ConversationMemberRepository memberRepository;

    public void requireConversationMember(String userId, String conversationId) {
        if (conversationId == null || !memberRepository.existsActiveMember(conversationId, userId)) {
            throw new ForbiddenException(ErrorMessage.Chat.ERR_NOT_MEMBER);
        }
    }

    public void authorizeSubscription(String userId, String destination) {
        if (USER_QUEUES.contains(destination == null ? "" : destination)) return;
        if (destination != null && destination.startsWith(CONVERSATION_TOPIC)) {
            requireConversationMember(userId, destination.substring(CONVERSATION_TOPIC.length()));
            return;
        }
        throw new ForbiddenException(ErrorMessage.FORBIDDEN);
    }

    public void authorizeSend(String destination) {
        if (destination == null || !SEND_DESTINATIONS.contains(destination)) {
            throw new ForbiddenException(ErrorMessage.FORBIDDEN);
        }
    }

    public void authorizeDelivery(String userId, String destination) {
        if (destination != null && destination.startsWith(CONVERSATION_TOPIC)) {
            requireConversationMember(userId, destination.substring(CONVERSATION_TOPIC.length()));
        }
    }
}
