package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.domain.dto.request.CancelBookingRequestDto;
import org.hit.chiikaiwabe.domain.dto.request.RateBookingRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;
import org.hit.chiikaiwabe.domain.entity.BookingRating;
import org.hit.chiikaiwabe.domain.entity.OfflineBooking;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.BookingStatus;
import org.hit.chiikaiwabe.domain.mapper.BookingMapper;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.config.properties.BookingProperties;
import org.hit.chiikaiwabe.repository.BookingRatingRepository;
import org.hit.chiikaiwabe.repository.OfflineBookingRepository;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.service.BookingLifecycleService;
import org.hit.chiikaiwabe.service.ChatNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingLifecycleServiceImpl implements BookingLifecycleService {

    private final OfflineBookingRepository bookingRepository;
    private final BookingRatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final ChatNotificationService chatNotificationService;
    private final BookingProperties bookingProperties;

    @Override
    @Transactional
    public BookingResponseDto cancelBooking(String userId, String bookingId, CancelBookingRequestDto dto) {
        OfflineBooking booking = getBookingAndVerifyInvolvement(userId, bookingId);

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidException(ErrorMessage.Booking.ERR_INVALID_STATUS);
        }

        LocalDateTime now = LocalDateTime.now();
        if (booking.getScheduledAt().minusMinutes(bookingProperties.getMinAdvanceMinutes()).isBefore(now)) {
            throw new InvalidException(ErrorMessage.Booking.ERR_CANCEL_TOO_LATE);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledBy(userId);
        booking.setCancelReason(dto.getCancelReason());

        booking = bookingRepository.save(booking);

        if (booking.getConversation() != null) {
            sendBookingNotification(booking.getConversation().getId(), bookingId, BookingStatus.CANCELLED);
        }

        return bookingMapper.toDto(booking, userId, false, null);
    }

    @Override
    @Transactional
    public BookingResponseDto completeBooking(String userId, String bookingId) {
        OfflineBooking booking = getBookingAndVerifyInvolvement(userId, bookingId);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidException(ErrorMessage.Booking.ERR_INVALID_STATUS);
        }

        LocalDateTime now = LocalDateTime.now();
        if (booking.getScheduledAt().isAfter(now)) {
            throw new InvalidException(ErrorMessage.Booking.ERR_COMPLETE_TOO_EARLY);
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking = bookingRepository.save(booking);

        if (booking.getConversation() != null) {
            sendBookingNotification(booking.getConversation().getId(), bookingId, BookingStatus.COMPLETED);
        }

        return bookingMapper.toDto(booking, userId, false, null);
    }

    @Override
    @Transactional
    public void ratePartner(String userId, String bookingId, RateBookingRequestDto dto) {
        OfflineBooking booking = getBookingAndVerifyInvolvement(userId, bookingId);

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new InvalidException(ErrorMessage.Booking.ERR_INVALID_STATUS);
        }

        if (ratingRepository.existsByBookingIdAndRaterId(bookingId, userId)) {
            throw new InvalidException(ErrorMessage.Booking.ERR_ALREADY_RATED);
        }

        User rater = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));

        User ratedUser = null;
        if (booking.getCreator().getId().equals(userId)) {
            ratedUser = booking.getParticipants().stream()
                    .filter(p -> !p.getUser().getId().equals(userId))
                    .map(p -> p.getUser())
                    .findFirst()
                    .orElseThrow(() -> new InvalidException(ErrorMessage.Booking.ERR_NOT_PARTNER));
        } else {
            ratedUser = booking.getCreator();
        }

        BookingRating rating = BookingRating.builder()
                .booking(booking)
                .rater(rater)
                .ratedUser(ratedUser)
                .score(dto.getScore())
                .build();
        ratingRepository.saveAndFlush(rating);

        userRepository.updateTrustScoreMovingAverage(ratedUser.getId(), dto.getScore());
    }

    private OfflineBooking getBookingAndVerifyInvolvement(String userId, String bookingId) {
        OfflineBooking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Booking.ERR_NOT_FOUND));

        boolean isCreator = booking.getCreator().getId().equals(userId);
        boolean isParticipant = booking.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(userId));

        if (!isCreator && !isParticipant) {
            throw new ForbiddenException(ErrorMessage.Booking.ERR_NOT_PARTICIPANT);
        }

        return booking;
    }

    private void sendBookingNotification(String conversationId, String bookingId, BookingStatus newStatus) {
        if (conversationId != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "BOOKING_UPDATE");
            payload.put("bookingId", bookingId);
            payload.put("status", newStatus.name());

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    chatNotificationService.broadcastRawEvent(conversationId, payload);
                }
            });
        }
    }
}
