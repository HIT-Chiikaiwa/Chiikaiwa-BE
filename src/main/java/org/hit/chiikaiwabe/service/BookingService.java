package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.request.CreateBookingRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;

public interface BookingService {
    BookingResponseDto createBooking(String userId, String conversationId, CreateBookingRequestDto requestDto);
    BookingResponseDto acceptBooking(String userId, String bookingId);
    BookingResponseDto rejectBooking(String userId, String bookingId);
}
