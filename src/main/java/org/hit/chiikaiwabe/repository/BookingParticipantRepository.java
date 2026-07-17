package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.BookingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingParticipantRepository extends JpaRepository<BookingParticipant, String> {

    Optional<BookingParticipant> findByBookingIdAndUserId(String bookingId, String userId);

}
