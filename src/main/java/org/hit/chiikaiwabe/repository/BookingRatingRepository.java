package org.hit.chiikaiwabe.repository;

import org.hit.chiikaiwabe.domain.entity.BookingRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRatingRepository extends JpaRepository<BookingRating, String> {

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM BookingRating r WHERE r.booking.id = :bookingId AND r.rater.id = :raterId")
    boolean existsByBookingIdAndRaterId(@Param("bookingId") String bookingId, @Param("raterId") String raterId);

    @Query("SELECT r FROM BookingRating r WHERE r.booking.id = :bookingId AND r.rater.id = :raterId")
    Optional<BookingRating> findByBookingIdAndRaterId(@Param("bookingId") String bookingId, @Param("raterId") String raterId);

    @Query("SELECT r FROM BookingRating r WHERE r.rater.id = :raterId AND r.booking.id IN :bookingIds")
    List<BookingRating> findByRaterIdAndBookingIds(@Param("raterId") String raterId,
                                                 @Param("bookingIds") Collection<String> bookingIds);

    @Query("SELECT AVG(r.score) FROM BookingRating r WHERE r.ratedUser.id = :userId")
    Double findAverageScoreByRatedUserId(@Param("userId") String userId);

}
