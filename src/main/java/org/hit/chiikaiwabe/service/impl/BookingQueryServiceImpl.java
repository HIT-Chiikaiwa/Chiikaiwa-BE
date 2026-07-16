package org.hit.chiikaiwabe.service.impl;

import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.BookingWeekResponseDto;
import org.hit.chiikaiwabe.domain.entity.OfflineBooking;
import org.hit.chiikaiwabe.domain.mapper.BookingMapper;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.repository.BookingParticipantRepository;
import org.hit.chiikaiwabe.repository.BookingRatingRepository;
import org.hit.chiikaiwabe.repository.OfflineBookingRepository;
import org.hit.chiikaiwabe.service.BookingQueryService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingQueryServiceImpl implements BookingQueryService {
    private final OfflineBookingRepository offlineBookingRepository;
    private final BookingParticipantRepository bookingParticipantRepository;
    private final BookingRatingRepository bookingRatingRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingResponseDto getBookingDetail(String bookingId, String currentUserId) {
        OfflineBooking booking = offlineBookingRepository.findById(bookingId)
                .orElseThrow(()-> new NotFoundException("Booking not found"));
        boolean isCreator = booking.getCreator() != null && booking.getCreator().getId().equals(currentUserId);
        boolean isPartner = booking.getParticipants() != null && booking.getParticipants().stream()
                .anyMatch(p -> p.getUser() != null && p.getUser().getId().equals(currentUserId));

        BookingResponseDto dto = bookingMapper.toDto(booking, currentUserId);
        boolean hasRated = bookingRatingRepository.existsByBookingIdAndRaterId(bookingId, currentUserId);

        dto.setHasRated(hasRated);
        return dto;
    }

    @Override
    public List<BookingResponseDto> getMyBookings(String currentUserId) {
        List<OfflineBooking> bookingList = offlineBookingRepository.findAllByUserId(currentUserId);
        return bookingMapper.toDtoList(bookingList, currentUserId);
    }

    @Override
    public BookingWeekResponseDto getWeeklySchedule(LocalDate weekStart, String currentUserId) {
        LocalDateTime startDate = weekStart.atStartOfDay();
        LocalDateTime endDate = weekStart.plusDays(6).atTime(LocalTime.MAX);

        List<OfflineBooking> bookings = offlineBookingRepository.findWeeklySchedule(currentUserId, startDate, endDate);
        List<BookingResponseDto> dtos = bookingMapper.toDtoList(bookings, currentUserId);
        Map<DayOfWeek, List<BookingResponseDto>> groupedByDay = dtos.stream()
                .collect(Collectors.groupingBy(dto -> dto.getScheduledAt().getDayOfWeek()));
        return BookingWeekResponseDto.builder().weekStart(weekStart)
                .weekEnd(endDate.toLocalDate())
                .daySchedules(groupedByDay).build();
    }
}
