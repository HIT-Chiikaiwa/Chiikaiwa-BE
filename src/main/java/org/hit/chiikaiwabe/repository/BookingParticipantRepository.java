package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.BookingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingParticipantRepository extends JpaRepository<BookingParticipant, String> {

    @Query("SELECT bp FROM BookingParticipant bp WHERE bp.booking.id = :bookingId AND bp.user.id = :userId")
    Optional<BookingParticipant> findByBookingIdAndUserId(@Param("bookingId") String bookingId, @Param("userId") String userId);

}
