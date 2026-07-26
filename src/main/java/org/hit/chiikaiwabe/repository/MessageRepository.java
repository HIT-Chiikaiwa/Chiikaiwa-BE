package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    @EntityGraph(attributePaths = {"sender", "replyToMessage"})
    @Query("SELECT m FROM Message m WHERE m.conversation.id = ?1 " +
            "AND m.id NOT IN (SELECT md.message.id FROM MessageDeletion md WHERE md.user.id = ?2) " +
            "AND (CAST(?3 AS timestamp) IS NULL OR m.createdDate <= ?3) " +
            "ORDER BY m.createdDate DESC")
    Page<Message> findByConversationIdForUser(String conversationId, String userId,
                                              LocalDateTime leftAt, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = ?1 " +
            "AND m.createdDate > ?2 " +
            "AND (m.sender.id IS NULL OR m.sender.id <> ?3)")
    int countUnreadMessages(String conversationId, LocalDateTime lastReadAt, String userId);

    @Query("SELECT m FROM Message m " +
            "LEFT JOIN FETCH m.sender " +
            "LEFT JOIN FETCH m.conversation " +
            "WHERE m.id = ?1")
    Optional<Message> findByIdWithDetails(String messageId);

    @EntityGraph(attributePaths = {"sender"})
    @Query("SELECT m FROM Message m WHERE m.conversation.id = ?1 " +
            "AND m.id NOT IN (SELECT md.message.id FROM MessageDeletion md WHERE md.user.id = ?2) " +
            "AND m.isRecalled = false " +
            "AND LOWER(m.content) LIKE LOWER(CONCAT('%', ?3, '%')) " +
            "ORDER BY m.createdDate DESC")
    Page<Message> searchMessages(String conversationId, String userId, String keyword, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = ?1 " +
            "AND m.isPinned = true " +
            "ORDER BY m.lastModifiedDate DESC")
    List<Message> findPinnedMessages(String conversationId);
}
