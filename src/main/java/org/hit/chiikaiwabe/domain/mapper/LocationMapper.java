package org.hit.chiikaiwabe.domain.mapper;

import org.hit.chiikaiwabe.domain.dto.response.NearbyUserDto;
import org.hit.chiikaiwabe.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    @Mapping(target = "latitude", ignore = true)
    @Mapping(target = "longitude", ignore = true)
    @Mapping(target = "distanceKm", ignore = true)
    @Mapping(target = "userId", source = "id")
    NearbyUserDto toNearbyUserDto(User user);

}
