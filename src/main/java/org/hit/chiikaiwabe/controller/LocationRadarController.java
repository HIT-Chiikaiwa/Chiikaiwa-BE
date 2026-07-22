package org.hit.chiikaiwabe.controller;

import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.constant.UrlConstant;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.domain.dto.request.GpsUpdateRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.NearbyUserDto;
import org.hit.chiikaiwabe.base.RestData;
import org.hit.chiikaiwabe.security.CurrentUser;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.LocationRadarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.hit.chiikaiwabe.annotation.RateLimit;

import jakarta.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestApiV1
@RateLimit(capacity = 5, durationInSeconds = 60)
public class LocationRadarController {

    private final LocationRadarService locationRadarService;

    @Tag(name = "location-radar-controller")
    @Operation(summary = "Scan nearby users (Radar)")
    @GetMapping(UrlConstant.Location.RADAR)
    public ResponseEntity<RestData<List<NearbyUserDto>>> scanRadar(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false, defaultValue = "5") Double radius) {
        return VsResponseUtil.success(locationRadarService.scanRadar(userPrincipal.getId(), lat, lng, radius));
    }

    @Tag(name = "location-radar-controller")
    @Operation(summary = "Update GPS location")
    @PutMapping(UrlConstant.Location.UPDATE_GPS)
    public ResponseEntity<RestData<CommonResponseDto>> updateLocation(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody GpsUpdateRequestDto dto) {
        locationRadarService.updateLocation(userPrincipal.getId(), dto.getLatitude(), dto.getLongitude());
        return VsResponseUtil.success(new CommonResponseDto(true, SuccessMessage.LOCATION_UPDATED));
    }

    @Tag(name = "location-radar-controller")
    @Operation(summary = "Remove GPS location")
    @DeleteMapping(UrlConstant.Location.REMOVE_GPS)
    public ResponseEntity<RestData<CommonResponseDto>> removeLocation(
            @CurrentUser UserPrincipal userPrincipal) {
        locationRadarService.removeLocation(userPrincipal.getId());
        return VsResponseUtil.success(new CommonResponseDto(true, SuccessMessage.LOCATION_REMOVED));
    }

}
