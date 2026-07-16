package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.request.CancelBookingRequestDto;
import org.hit.chiikaiwabe.domain.dto.request.RateBookingRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;

public interface BookingLifecycleService {

    BookingResponseDto cancelBooking(String userId, String bookingId, CancelBookingRequestDto dto);

    BookingResponseDto completeBooking(String userId, String bookingId);

    void ratePartner(String userId, String bookingId, RateBookingRequestDto dto);

}
