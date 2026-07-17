package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.hit.chiikaiwabe.domain.enums.BookingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {

    @EntityGraph(attributePaths = "participants")
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdWithParticipants(@Param("id") String id);

    long countByConversationIdAndStatusIn(String conversationId, List<BookingStatus> statuses);
}
