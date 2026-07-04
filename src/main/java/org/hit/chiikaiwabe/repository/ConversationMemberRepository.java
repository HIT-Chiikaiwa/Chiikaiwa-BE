package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, String> {
    Optional<ConversationMember> findByConversationIdAndUserId(String conversationId, String userId);
}
