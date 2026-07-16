package org.hit.chiikaiwabe.service.impl;

import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.BookingWeekResponseDto;
import org.hit.chiikaiwabe.domain.entity.OfflineBooking;
import org.hit.chiikaiwabe.domain.mapper.BookingMapper;
import org.hit.chiikaiwabe.repository.BookingParticipantRepository;
import org.hit.chiikaiwabe.repository.BookingRatingRepository;
import org.hit.chiikaiwabe.repository.OfflineBookingRepository;
import org.hit.chiikaiwabe.service.BookingQueryService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingQueryServiceImpl implements BookingQueryService {
    private final OfflineBookingRepository offlineBookingRepository;
    private final BookingParticipantRepository bookingParticipantRepository;
    private final BookingRatingRepository bookingRatingRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingResponseDto getBookingDetail(String bookingId, String currentUserId) {
        OfflineBooking booking =
        return null;
    }

    @Override
    public List<BookingResponseDto> getMyBookings(String currentUserId) {
        return List.of();
    }

    @Override
    public BookingWeekResponseDto getWeeklySchedule(LocalDate weekStart, String currentUserId) {
        return null;
    }
}
