package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.BookingRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRatingRepository extends JpaRepository<BookingRating, String> {

    boolean existsByBookingIdAndRaterId(String bookingId, String raterId);

    Optional<BookingRating> findByBookingIdAndRaterId(String bookingId, String raterId);

    @Query("SELECT AVG(r.score) FROM BookingRating r WHERE r.ratedUser.id = :userId")
    Double findAverageScoreByRatedUserId(@Param("userId") String userId);

}
