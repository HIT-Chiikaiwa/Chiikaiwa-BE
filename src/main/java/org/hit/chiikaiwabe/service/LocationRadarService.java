package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.response.NearbyUserDto;

import java.util.List;

public interface LocationRadarService {


    List<NearbyUserDto> scanRadar(String userId, double lat, double lng, Double radiusKm);


}
