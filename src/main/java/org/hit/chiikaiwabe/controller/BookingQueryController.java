package org.hit.chiikaiwabe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.RestData;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.BookingWeekResponseDto;
import org.hit.chiikaiwabe.security.CurrentUser;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.BookingQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.hit.chiikaiwabe.annotation.RateLimit;

import java.time.LocalDate;
import java.util.List;

@RestController
@RestApiV1
@RequiredArgsConstructor
@Tag(name = "booking-query-controller")
@RateLimit(capacity = 30, durationInSeconds = 60)
public class BookingQueryController {
    private final BookingQueryService bookingQueryService;

    @Operation(summary = "Get booking detail")
    @GetMapping(org.hit.chiikaiwabe.constant.UrlConstant.Booking.DETAIL)
    public ResponseEntity<RestData<BookingResponseDto>> getBookingDetail(
            @PathVariable String bookingId,
            @CurrentUser UserPrincipal currentUser
    ){
        BookingResponseDto responseDto = bookingQueryService.getBookingDetail(currentUser.getId(), bookingId);
        return VsResponseUtil.success(responseDto);
    }

    @Operation(summary = "Get my bookings")
    @GetMapping(org.hit.chiikaiwabe.constant.UrlConstant.Booking.LIST)
    public ResponseEntity<RestData<List<BookingResponseDto>>> getMyBookings(@CurrentUser UserPrincipal currentUser){
        List<BookingResponseDto> response = bookingQueryService.getMyBookings(currentUser.getId());
        return VsResponseUtil.success(response);
    }

    @Operation(summary = "Get weekly schedule")
    @GetMapping(org.hit.chiikaiwabe.constant.UrlConstant.Booking.WEEKLY)
    public ResponseEntity<RestData<BookingWeekResponseDto>> getWeeklySchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @CurrentUser UserPrincipal currentUser){
        BookingWeekResponseDto response = bookingQueryService.getWeeklySchedule(currentUser.getId(), weekStart);
        return VsResponseUtil.success(response);
    }
}
