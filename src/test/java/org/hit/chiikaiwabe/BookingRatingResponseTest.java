package org.hit.chiikaiwabe;

import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;
import org.hit.chiikaiwabe.domain.entity.BookingParticipant;
import org.hit.chiikaiwabe.domain.entity.BookingRating;
import org.hit.chiikaiwabe.domain.entity.OfflineBooking;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.BookingStatus;
import org.hit.chiikaiwabe.domain.enums.ParticipantStatus;
import org.hit.chiikaiwabe.domain.mapper.BookingMapper;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.repository.BookingRatingRepository;
import org.hit.chiikaiwabe.repository.OfflineBookingRepository;
import org.hit.chiikaiwabe.service.impl.BookingQueryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookingRatingResponseTest {
    private OfflineBookingRepository bookings;
    private BookingRatingRepository ratings;
    private BookingQueryServiceImpl service;
    private OfflineBooking booking;

    @BeforeEach
    void setUp() {
        bookings = mock(OfflineBookingRepository.class);
        ratings = mock(BookingRatingRepository.class);
        service = new BookingQueryServiceImpl(bookings, ratings, Mappers.getMapper(BookingMapper.class));
        booking = booking("booking-1");
    }

    private OfflineBooking booking(String id) {
        return OfflineBooking.builder().id(id).status(BookingStatus.COMPLETED)
                .creator(User.builder().id("creator").build())
                .scheduledAt(LocalDate.of(2026, 11, 15).atTime(10, 0))
                .participants(List.of(BookingParticipant.builder()
                        .user(User.builder().id("invitee").build()).status(ParticipantStatus.ACCEPTED).build()))
                .build();
    }

    private BookingRating rating(OfflineBooking target, int score) {
        return BookingRating.builder().booking(target).score(score).build();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void detailReturnsExactSubmittedScore(int score) {
        when(bookings.findByIdWithDetails("booking-1")).thenReturn(Optional.of(booking));
        when(ratings.findByBookingIdAndRaterId("booking-1", "creator"))
                .thenReturn(Optional.of(rating(booking, score)));
        assertRating(service.getBookingDetail("creator", "booking-1"), score);
    }

    @Test
    void eachPartySeesTheirOwnScore() {
        when(bookings.findByIdWithDetails("booking-1")).thenReturn(Optional.of(booking));
        when(ratings.findByBookingIdAndRaterId("booking-1", "creator"))
                .thenReturn(Optional.of(rating(booking, 2)));
        when(ratings.findByBookingIdAndRaterId("booking-1", "invitee"))
                .thenReturn(Optional.of(rating(booking, 5)));
        assertRating(service.getBookingDetail("creator", "booking-1"), 2);
        assertRating(service.getBookingDetail("invitee", "booking-1"), 5);
    }

    @Test
    void detailWithoutRatingReturnsFalseAndNull() {
        when(bookings.findByIdWithDetails("booking-1")).thenReturn(Optional.of(booking));
        when(ratings.findByBookingIdAndRaterId("booking-1", "creator")).thenReturn(Optional.empty());
        assertRating(service.getBookingDetail("creator", "booking-1"), null);
    }

    @Test
    void listUsesOneBatchQueryAndMatchesScoresByBookingId() {
        OfflineBooking second = booking("booking-2");
        OfflineBooking unrated = booking("booking-3");
        when(bookings.findAllByUserId("creator")).thenReturn(List.of(booking, second, unrated));
        when(ratings.findByRaterIdAndBookingIds("creator", List.of("booking-1", "booking-2", "booking-3")))
                .thenReturn(List.of(rating(second, 5), rating(booking, 3)));
        List<BookingResponseDto> result = service.getMyBookings("creator");
        assertRating(result.get(0), 3);
        assertRating(result.get(1), 5);
        assertRating(result.get(2), null);
        verify(ratings).findByRaterIdAndBookingIds("creator", List.of("booking-1", "booking-2", "booking-3"));
        verifyNoMoreInteractions(ratings);
    }

    @Test
    void weeklyScheduleIncludesRatingAndUnratedState() {
        LocalDate start = LocalDate.of(2026, 11, 9);
        when(bookings.findWeeklySchedule(eq("invitee"), any(), any()))
                .thenReturn(List.of(booking, booking("booking-2")));
        when(ratings.findByRaterIdAndBookingIds("invitee", List.of("booking-1", "booking-2")))
                .thenReturn(List.of(rating(booking, 4)));
        var schedule = service.getWeeklySchedule("invitee", start);
        var result = schedule.getDaySchedules().get(booking.getScheduledAt().getDayOfWeek());
        assertRating(result.get(0), 4);
        assertRating(result.get(1), null);
    }

    @Test
    void emptyResultsDoNotQueryRatings() {
        when(bookings.findAllByUserId("creator")).thenReturn(List.of());
        when(bookings.findWeeklySchedule(eq("creator"), any(), any())).thenReturn(List.of());
        assertThat(service.getMyBookings("creator")).isEmpty();
        assertThat(service.getWeeklySchedule("creator", LocalDate.of(2026, 11, 9)).getDaySchedules()).isEmpty();
        verifyNoInteractions(ratings);
    }

    @Test
    void outsiderCannotReadBookingOrRatings() {
        when(bookings.findByIdWithDetails("booking-1")).thenReturn(Optional.of(booking));
        assertThatThrownBy(() -> service.getBookingDetail("outsider", "booking-1"))
                .isInstanceOf(ForbiddenException.class);
        verifyNoInteractions(ratings);
    }

    private void assertRating(BookingResponseDto result, Integer score) {
        assertThat(result.getHasRated()).isEqualTo(score != null);
        assertThat(result.getMyRating()).isEqualTo(score);
    }
}
