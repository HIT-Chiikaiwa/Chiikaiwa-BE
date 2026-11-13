package org.hit.chiikaiwabe;

import org.hit.chiikaiwabe.component.ChatHelper;
import org.hit.chiikaiwabe.config.properties.BookingProperties;
import org.hit.chiikaiwabe.domain.dto.request.CancelBookingRequestDto;
import org.hit.chiikaiwabe.domain.entity.BookingParticipant;
import org.hit.chiikaiwabe.domain.entity.OfflineBooking;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.BookingStatus;
import org.hit.chiikaiwabe.domain.enums.ParticipantStatus;
import org.hit.chiikaiwabe.domain.enums.PointAction;
import org.hit.chiikaiwabe.domain.mapper.BookingMapper;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.repository.BookingParticipantRepository;
import org.hit.chiikaiwabe.repository.BookingRatingRepository;
import org.hit.chiikaiwabe.repository.OfflineBookingRepository;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.service.*;
import org.hit.chiikaiwabe.service.impl.BookingLifecycleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.MessageSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingLifecyclePermissionTest {
    private OfflineBookingRepository repository;
    private LeaderboardService leaderboard;
    private BookingLifecycleService service;
    private OfflineBooking booking;

    @BeforeEach
    void setUp() {
        repository = mock(OfflineBookingRepository.class);
        leaderboard = mock(LeaderboardService.class);
        BookingProperties properties = new BookingProperties();
        properties.setMinAdvanceMinutes(30);
        service = new BookingLifecycleServiceImpl(repository, mock(BookingRatingRepository.class),
                mock(UserRepository.class), mock(BookingMapper.class), mock(ChatNotificationService.class),
                properties, mock(BookingParticipantRepository.class), mock(PushNotificationService.class),
                mock(ChatHelper.class), leaderboard, mock(NotificationService.class), mock(MessageSource.class));
        booking = OfflineBooking.builder().id("booking-1")
                .creator(User.builder().id("creator").build())
                .participants(List.of(BookingParticipant.builder()
                        .user(User.builder().id("invitee").build()).status(ParticipantStatus.ACCEPTED).build()))
                .status(BookingStatus.CONFIRMED).scheduledAt(LocalDateTime.now().plusDays(1)).build();
        when(repository.findByIdWithDetails("booking-1")).thenReturn(Optional.of(booking));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"creator", "invitee"})
    void creatorAndInviteeCanCancel(String actor) {
        service.cancelBooking(actor, "booking-1", new CancelBookingRequestDto("Cannot attend"));
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getCancelledBy()).isEqualTo(actor);
        assertThat(booking.getCancelReason()).isEqualTo("Cannot attend");
        verify(repository).save(booking);
        verify(leaderboard).awardPoints(actor, PointAction.BOOKING_CANCELLED, "booking-1");
    }

    @ParameterizedTest
    @ValueSource(strings = {"creator", "invitee"})
    void creatorAndInviteeCanComplete(String actor) {
        booking.setScheduledAt(LocalDateTime.now().minusHours(1));
        service.completeBooking(actor, "booking-1");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
        verify(repository).save(booking);
        verify(leaderboard).awardPoints("creator", PointAction.BOOKING_COMPLETED, "booking-1");
        verify(leaderboard).awardPoints("invitee", PointAction.BOOKING_COMPLETED, "booking-1");
    }

    @ParameterizedTest
    @ValueSource(strings = {"cancel", "complete"})
    void unrelatedUserCannotChangeBooking(String action) {
        assertThatThrownBy(() -> perform(action, "outsider")).isInstanceOf(ForbiddenException.class);
        verify(repository, never()).save(any());
        verifyNoInteractions(leaderboard);
    }

    @ParameterizedTest
    @ValueSource(strings = {"cancel", "complete"})
    void creatorStillCannotChangeFinalBooking(String action) {
        booking.setStatus(BookingStatus.CANCELLED);
        assertThatThrownBy(() -> perform(action, "creator")).isInstanceOf(InvalidException.class);
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"creator", "invitee"})
    void neitherPartyCanCompleteBeforeScheduledTime(String actor) {
        assertThatThrownBy(() -> service.completeBooking(actor, "booking-1"))
                .isInstanceOf(InvalidException.class);
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"creator", "invitee"})
    void neitherPartyCanCancelAfterDeadline(String actor) {
        booking.setScheduledAt(LocalDateTime.now().plusMinutes(10));
        assertThatThrownBy(() -> perform("cancel", actor)).isInstanceOf(InvalidException.class);
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"cancel", "complete"})
    void missingBookingIsRejected(String action) {
        when(repository.findByIdWithDetails("booking-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> perform(action, "creator")).isInstanceOf(NotFoundException.class);
        verify(repository, never()).save(any());
    }

    private void perform(String action, String actor) {
        if (action.equals("cancel")) {
            service.cancelBooking(actor, "booking-1", new CancelBookingRequestDto("Cannot attend"));
        } else {
            service.completeBooking(actor, "booking-1");
        }
    }
}
