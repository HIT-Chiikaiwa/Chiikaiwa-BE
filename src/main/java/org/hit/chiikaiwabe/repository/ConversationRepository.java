package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    @Query("SELECT c FROM Conversation c " +
            "WHERE c.id IN (" +
            "  SELECT cm.conversation.id FROM ConversationMember cm " +
            "  WHERE cm.user.id = ?1 AND cm.leftAt IS NULL" +
            ") ORDER BY c.lastModifiedDate DESC")
    Page<Conversation> findAllByUserId(String userId, Pageable pageable);

    @Query("SELECT c FROM Conversation c " +
            "LEFT JOIN FETCH c.lastMessage " +
            "LEFT JOIN FETCH c.createdBy " +
            "WHERE c.id IN ?1")
    List<Conversation> findAllWithLastMessageByIds(List<String> ids);

    @Query("SELECT c FROM Conversation c WHERE c.type = 'DIRECT' AND c.id IN (" +
            "  SELECT cm1.conversation.id FROM ConversationMember cm1 " +
            "  WHERE cm1.user.id = ?1 AND cm1.leftAt IS NULL" +
            ") AND c.id IN (" +
            "  SELECT cm2.conversation.id FROM ConversationMember cm2 " +
            "  WHERE cm2.user.id = ?2 AND cm2.leftAt IS NULL" +
            ")")
    Optional<Conversation> findDirectConversation(String userId1, String userId2);

    @Query("SELECT DISTINCT c FROM Conversation c " +
            "JOIN ConversationMember cm ON cm.conversation.id = c.id " +
            "WHERE cm.user.id = ?1 AND cm.leftAt IS NULL " +
            "AND (" +
            "  (c.type = 'GROUP' AND LOWER(c.groupName) LIKE LOWER(CONCAT('%', ?2, '%'))) " +
            "  OR (c.type = 'DIRECT' AND c.id IN (" +
            "    SELECT cm2.conversation.id FROM ConversationMember cm2 " +
            "    WHERE cm2.conversation.id = c.id AND cm2.user.id <> ?1 " +
            "    AND (LOWER(cm2.user.firstName) LIKE LOWER(CONCAT('%', ?2, '%')) " +
            "         OR LOWER(cm2.user.lastName) LIKE LOWER(CONCAT('%', ?2, '%')))" +
            "  ))" +
            ") ORDER BY c.lastModifiedDate DESC")
    Page<Conversation> searchByKeyword(String userId, String keyword, Pageable pageable);
}
