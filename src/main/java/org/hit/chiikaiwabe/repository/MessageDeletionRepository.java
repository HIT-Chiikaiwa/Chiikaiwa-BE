package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.MessageDeletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageDeletionRepository extends JpaRepository<MessageDeletion, String> {
    Optional<MessageDeletion> findByMessageIdAndUserId(String messageId, String userId);
}
