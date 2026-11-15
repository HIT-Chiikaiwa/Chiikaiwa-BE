package org.hit.chiikaiwabe.service.impl;

import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.BookingWeekResponseDto;
import org.hit.chiikaiwabe.domain.entity.OfflineBooking;
import org.hit.chiikaiwabe.domain.entity.BookingRating;
import org.hit.chiikaiwabe.domain.mapper.BookingMapper;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.repository.BookingRatingRepository;
import org.hit.chiikaiwabe.repository.OfflineBookingRepository;
import org.hit.chiikaiwabe.service.BookingQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingQueryServiceImpl implements BookingQueryService {
    private final OfflineBookingRepository offlineBookingRepository;
    private final BookingRatingRepository bookingRatingRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingResponseDto getBookingDetail(String currentUserId, String bookingId) {
        OfflineBooking booking = offlineBookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(()-> new NotFoundException(ErrorMessage.Booking.ERR_NOT_FOUND));
        boolean isCreator = booking.getCreator() != null && booking.getCreator().getId().equals(currentUserId);
        boolean isPartner = booking.getParticipants() != null && booking.getParticipants().stream()
                .anyMatch(p -> p.getUser() != null && p.getUser().getId().equals(currentUserId));

        if (!isCreator && !isPartner) {
            throw new ForbiddenException(ErrorMessage.Booking.ERR_NOT_PARTICIPANT);
        }

        BookingResponseDto dto = bookingMapper.toDto(booking, currentUserId);
        Integer score = bookingRatingRepository.findByBookingIdAndRaterId(bookingId, currentUserId)
                .map(BookingRating::getScore).orElse(null);
        setRating(dto, score);
        return dto;
    }

    @Override
    public List<BookingResponseDto> getMyBookings(String currentUserId) {
        List<OfflineBooking> bookingList = offlineBookingRepository.findAllByUserId(currentUserId);
        List<BookingResponseDto> dtos = bookingMapper.toDtoList(bookingList, currentUserId);
        populateRatings(dtos, currentUserId);
        return dtos;
    }

    @Override
    public BookingWeekResponseDto getWeeklySchedule(String currentUserId, LocalDate weekStart) {
        LocalDateTime startDate = weekStart.atStartOfDay();
        LocalDateTime endDate = weekStart.plusDays(6).atTime(LocalTime.MAX);

        List<OfflineBooking> bookings = offlineBookingRepository.findWeeklySchedule(currentUserId, startDate, endDate);
        List<BookingResponseDto> dtos = bookingMapper.toDtoList(bookings, currentUserId);
        populateRatings(dtos, currentUserId);
        Map<DayOfWeek, List<BookingResponseDto>> groupedByDay = dtos.stream()
                .collect(Collectors.groupingBy(dto -> dto.getScheduledAt().getDayOfWeek()));
        return BookingWeekResponseDto.builder().weekStart(weekStart)
                .weekEnd(endDate.toLocalDate())
                .daySchedules(groupedByDay).build();
    }

    private void populateRatings(List<BookingResponseDto> dtos, String currentUserId) {
        if (dtos.isEmpty()) return;
        List<String> bookingIds = dtos.stream().map(BookingResponseDto::getId).toList();
        Map<String, Integer> scores = bookingRatingRepository.findByRaterIdAndBookingIds(currentUserId, bookingIds)
                .stream().collect(Collectors.toMap(rating -> rating.getBooking().getId(), BookingRating::getScore));
        dtos.forEach(dto -> setRating(dto, scores.get(dto.getId())));
    }

    private void setRating(BookingResponseDto dto, Integer score) {
        dto.setHasRated(score != null);
        dto.setMyRating(score);
    }

}
