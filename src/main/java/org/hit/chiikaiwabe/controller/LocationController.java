package org.hit.chiikaiwabe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.constant.UrlConstant;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.domain.dto.request.GpsLocationRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.security.CurrentUser;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RestApiV1
@Tag(name = "location-controller")
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "API update location")
    @PutMapping(UrlConstant.Location.UPDATE)
    public ResponseEntity<?> updateLocation(
            @Parameter(name = "principal", hidden = true) @CurrentUser UserPrincipal principal,
            @Valid @RequestBody GpsLocationRequestDto requestDto) {
        
        locationService.updateLocation(principal.getId(), requestDto.getLatitude(), requestDto.getLongitude());
        
        return VsResponseUtil.success(new CommonResponseDto(true, SuccessMessage.LOCATION_UPDATED));
    }
}
