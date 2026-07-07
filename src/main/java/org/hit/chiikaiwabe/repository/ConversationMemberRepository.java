package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, String> {

    @Query("SELECT cm FROM ConversationMember cm WHERE cm.conversation.id = ?1 AND cm.user.id = ?2")
    Optional<ConversationMember> findByConversationIdAndUserId(String conversationId, String userId);

    @Query("SELECT cm FROM ConversationMember cm WHERE cm.conversation.id = ?1 AND cm.leftAt IS NULL")
    List<ConversationMember> findActiveMembers(String conversationId);

    @Query("SELECT COUNT(cm) FROM ConversationMember cm WHERE cm.conversation.id = ?1 AND cm.leftAt IS NULL")
    int countActiveMembers(String conversationId);

    @Query("SELECT cm.user.id FROM ConversationMember cm WHERE cm.conversation.id = ?1 AND cm.leftAt IS NULL")
    List<String> findActiveUserIds(String conversationId);
}
