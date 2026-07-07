package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    @Query("SELECT m FROM Message m WHERE m.conversation.id = ?1 " +
            "AND m.id NOT IN (SELECT md.message.id FROM MessageDeletion md WHERE md.user.id = ?2) " +
            "AND (?3 IS NULL OR m.createdDate <= ?3) " +
            "ORDER BY m.createdDate DESC")
    Page<Message> findByConversationIdForUser(String conversationId, String userId,
                                              LocalDateTime leftAt, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = ?1 " +
            "AND m.createdDate > ?2 " +
            "AND (m.sender.id IS NULL OR m.sender.id <> ?3)")
    int countUnreadMessages(String conversationId, LocalDateTime lastReadAt, String userId);
}
