package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.BookingWeekResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface BookingQueryService {

    BookingResponseDto getBookingDetail(String userId, String bookingId);

    List<BookingResponseDto> getMyBookings(String userId);

    BookingWeekResponseDto getWeeklySchedule(String userId, LocalDate weekStart);

}
