package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    @Query("SELECT n FROM Notification n JOIN FETCH n.actor " +
            "WHERE n.recipient.id = :userId ORDER BY n.createdDate DESC")
    Page<Notification> findByRecipientId(@Param("userId") String userId, Pageable pageable);

    long countByRecipientIdAndIsReadFalse(String recipientId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true " +
            "WHERE n.recipient.id = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipient.id = :userId")
    int deleteAllByRecipientId(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.id IN :ids AND n.recipient.id = :userId")
    int deleteByIdInAndRecipientId(@Param("ids") List<String> ids, @Param("userId") String userId);
}
