package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, String> {

    List<MessageReaction> findByMessageId(String messageId);

    Optional<MessageReaction> findByMessageIdAndUserId(String messageId, String userId);

    void deleteByMessageIdAndUserId(String messageId, String userId);

    @Query("SELECT r.emoji, COUNT(r) FROM MessageReaction r WHERE r.message.id = ?1 GROUP BY r.emoji")
    List<Object[]> countByMessageIdGroupByEmoji(String messageId);
}
