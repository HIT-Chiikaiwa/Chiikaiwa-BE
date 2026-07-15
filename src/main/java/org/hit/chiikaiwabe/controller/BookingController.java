package org.hit.chiikaiwabe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.domain.dto.request.CreateBookingRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;
import org.hit.chiikaiwabe.security.CurrentUser;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.BookingService;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.constant.UrlConstant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestApiV1
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Tag(name = "booking-controller")
    @Operation(summary = "Create booking in conversation")
    @PostMapping(UrlConstant.Booking.CREATE_BOOKING)
    public ResponseEntity<?> createBooking(@Parameter(hidden = true) @CurrentUser UserPrincipal principal,
                                           @PathVariable String conversationId,
                                           @RequestBody @Valid CreateBookingRequestDto requestDto) {
        return VsResponseUtil.success(bookingService.createBooking(principal.getId(), conversationId, requestDto));
    }

    @Tag(name = "booking-controller")
    @Operation(summary = "Accept booking request")
    @PutMapping(UrlConstant.Booking.ACCEPT_BOOKING)
    public ResponseEntity<?> acceptBooking(@Parameter(hidden = true) @CurrentUser UserPrincipal principal,
                                           @PathVariable String bookingId) {
        return VsResponseUtil.success(bookingService.acceptBooking(principal.getId(), bookingId));
    }

    @Tag(name = "booking-controller")
    @Operation(summary = "Reject booking request")
    @PutMapping(UrlConstant.Booking.REJECT_BOOKING)
    public ResponseEntity<?> rejectBooking(@Parameter(hidden = true) @CurrentUser UserPrincipal principal,
                                           @PathVariable String bookingId) {
        return VsResponseUtil.success(bookingService.rejectBooking(principal.getId(), bookingId));
    }
}
