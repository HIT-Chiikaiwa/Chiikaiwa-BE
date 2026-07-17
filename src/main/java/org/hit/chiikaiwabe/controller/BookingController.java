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
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.constant.UrlConstant;
import org.hit.chiikaiwabe.domain.dto.request.CancelBookingRequestDto;
import org.hit.chiikaiwabe.domain.dto.request.RateBookingRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.base.RestData;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.security.CurrentUser;
import org.hit.chiikaiwabe.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Parameter;
import org.hit.chiikaiwabe.service.BookingLifecycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Booking", description = "Offline Booking API")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingLifecycleService lifecycleService;

    @PatchMapping(UrlConstant.Booking.CANCEL)
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<RestData<BookingResponseDto>> cancelBooking(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PathVariable String bookingId,
            @Valid @RequestBody CancelBookingRequestDto dto) {
        BookingResponseDto response = lifecycleService.cancelBooking(principal.getId(), bookingId, dto);
        return VsResponseUtil.success(HttpStatus.OK, response);
    }

    @PatchMapping(UrlConstant.Booking.COMPLETE)
    @Operation(summary = "Complete a confirmed booking")
    public ResponseEntity<RestData<BookingResponseDto>> completeBooking(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PathVariable String bookingId) {
        BookingResponseDto response = lifecycleService.completeBooking(principal.getId(), bookingId);
        return VsResponseUtil.success(HttpStatus.OK, response);
    }

    @PostMapping(UrlConstant.Booking.RATE)
    @Operation(summary = "Rate partner after booking completion")
    public ResponseEntity<RestData<CommonResponseDto>> ratePartner(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PathVariable String bookingId,
            @Valid @RequestBody RateBookingRequestDto dto) {
        lifecycleService.ratePartner(principal.getId(), bookingId, dto);
        return VsResponseUtil.success(HttpStatus.OK, new CommonResponseDto(true, SuccessMessage.Booking.RATED));
    }

}
