package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.BookingWeekResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface BookingQueryService{
    BookingResponseDto getBookingDetail(String bookingId, String currentUserId);
    List<BookingResponseDto> getMyBookings(String currentUserId);
    BookingWeekResponseDto getWeeklySchedule(LocalDate weekStart, String currentUserId);
}
