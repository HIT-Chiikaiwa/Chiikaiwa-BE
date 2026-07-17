package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.OfflineBooking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfflineBookingRepository extends JpaRepository<OfflineBooking, String> {

    @EntityGraph(attributePaths = {"creator", "participants", "participants.user"})
    @Query("SELECT b FROM OfflineBooking b WHERE b.id = :id")
    Optional<OfflineBooking> findByIdWithDetails(@Param("id") String id);

    @Query("SELECT COUNT(b) FROM OfflineBooking b WHERE " +
            "(b.creator.id = :userId OR EXISTS (SELECT bp FROM BookingParticipant bp WHERE bp.booking = b AND bp.user.id = :userId AND bp.status = 'ACCEPTED')) " +
            "AND b.status IN ('PENDING', 'CONFIRMED')")
    long countActiveBookingsByUserId(@Param("userId") String userId);

    @EntityGraph(attributePaths = {"creator", "participants", "participants.user"})
    @Query("SELECT b FROM OfflineBooking b WHERE " +
            "b.creator.id = :userId OR EXISTS (SELECT bp FROM BookingParticipant bp WHERE bp.booking = b AND bp.user.id = :userId) " +
            "ORDER BY b.scheduledAt DESC")
    List<OfflineBooking> findAllByUserId(@Param("userId") String userId);

    @EntityGraph(attributePaths = {"creator", "participants", "participants.user"})
    @Query("SELECT b FROM OfflineBooking b WHERE " +
            "(b.creator.id = :userId OR EXISTS (SELECT bp FROM BookingParticipant bp WHERE bp.booking = b AND bp.user.id = :userId)) " +
            "AND b.scheduledAt BETWEEN :startDate AND :endDate " +
            "AND b.status NOT IN ('REJECTED', 'CANCELLED', 'EXPIRED') " +
            "ORDER BY b.scheduledAt ASC")
    List<OfflineBooking> findWeeklySchedule(@Param("userId") String userId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM OfflineBooking b WHERE " +
            "b.status = 'PENDING' AND b.createdDate < :expireThreshold")
    List<OfflineBooking> findExpiredPending(@Param("expireThreshold") LocalDateTime expireThreshold);

    @Query("SELECT b FROM OfflineBooking b WHERE " +
            "b.status = 'CONFIRMED' AND b.scheduledAt BETWEEN :start AND :end")
    List<OfflineBooking> findUpcomingForReminder(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);

    @Query("SELECT b FROM OfflineBooking b WHERE " +
            "b.status = 'CONFIRMED' AND b.scheduledAt < :expireThreshold")
    List<OfflineBooking> findExpiredConfirmed(@Param("expireThreshold") LocalDateTime expireThreshold);

}
