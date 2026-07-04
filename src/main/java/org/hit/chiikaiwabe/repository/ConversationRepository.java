package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
}
