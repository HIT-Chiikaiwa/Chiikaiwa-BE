package org.hit.chiikaiwabe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.base.RestData;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.constant.UrlConstant;
import org.hit.chiikaiwabe.domain.dto.request.CancelBookingRequestDto;
import org.hit.chiikaiwabe.domain.dto.request.CreateBookingRequestDto;
import org.hit.chiikaiwabe.domain.dto.request.RateBookingRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.security.CurrentUser;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.BookingLifecycleService;
import org.hit.chiikaiwabe.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Booking", description = "Offline Booking API")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;
    private final BookingLifecycleService lifecycleService;

    @Operation(summary = "Create booking in conversation")
    @PostMapping(UrlConstant.Booking.CREATE)
    public ResponseEntity<RestData<BookingResponseDto>> createBooking(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PathVariable String conversationId,
            @RequestBody @Valid CreateBookingRequestDto requestDto) {
        BookingResponseDto response = bookingService.createBooking(principal.getId(), conversationId, requestDto);
        return VsResponseUtil.success(HttpStatus.CREATED, response);
    }

    @Operation(summary = "Accept booking request")
    @PutMapping(UrlConstant.Booking.ACCEPT)
    public ResponseEntity<RestData<BookingResponseDto>> acceptBooking(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PathVariable String bookingId) {
        BookingResponseDto response = bookingService.acceptBooking(principal.getId(), bookingId);
        return VsResponseUtil.success(HttpStatus.OK, response);
    }

    @Operation(summary = "Reject booking request")
    @PutMapping(UrlConstant.Booking.REJECT)
    public ResponseEntity<RestData<BookingResponseDto>> rejectBooking(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PathVariable String bookingId) {
        BookingResponseDto response = bookingService.rejectBooking(principal.getId(), bookingId);
        return VsResponseUtil.success(HttpStatus.OK, response);
    }

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
