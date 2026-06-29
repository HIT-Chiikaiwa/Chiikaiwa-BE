package org.hit.chiikaiwabe.controller;

import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.constant.UrlConstant;
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

import java.util.List;

@RequiredArgsConstructor
@RestApiV1
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

}
