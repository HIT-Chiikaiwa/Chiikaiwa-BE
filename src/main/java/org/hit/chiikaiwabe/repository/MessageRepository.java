package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    @EntityGraph(attributePaths = {"sender", "replyToMessage"})
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "AND NOT EXISTS (SELECT 1 FROM MessageDeletion md WHERE md.message.id = m.id AND md.user.id = :userId) " +
            "AND (:leftAt IS NULL OR m.createdDate <= :leftAt) " +
            "ORDER BY m.createdDate DESC")
    Page<Message> findByConversationIdForUser(@Param("conversationId") String conversationId,
                                              @Param("userId") String userId,
                                              @Param("leftAt") LocalDateTime leftAt,
                                              Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId " +
            "AND m.createdDate > :lastReadAt " +
            "AND (m.sender.id IS NULL OR m.sender.id <> :userId)")
    int countUnreadMessages(@Param("conversationId") String conversationId,
                            @Param("lastReadAt") LocalDateTime lastReadAt,
                            @Param("userId") String userId);

    @Query("SELECT m FROM Message m " +
            "LEFT JOIN FETCH m.sender " +
            "LEFT JOIN FETCH m.conversation " +
            "WHERE m.id = :messageId")
    Optional<Message> findByIdWithDetails(@Param("messageId") String messageId);

    @EntityGraph(attributePaths = {"sender"})
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "AND NOT EXISTS (SELECT 1 FROM MessageDeletion md WHERE md.message.id = m.id AND md.user.id = :userId) " +
            "AND m.isRecalled = false " +
            "AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY m.createdDate DESC")
    Page<Message> searchMessages(@Param("conversationId") String conversationId,
                                 @Param("userId") String userId,
                                 @Param("keyword") String keyword,
                                 Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "AND m.isPinned = true " +
            "ORDER BY m.lastModifiedDate DESC")
    List<Message> findPinnedMessages(@Param("conversationId") String conversationId);
}

