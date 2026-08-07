package org.hit.chiikaiwabe.domain.mapper;

import org.hit.chiikaiwabe.domain.dto.response.NotificationResponseDto;
import org.hit.chiikaiwabe.domain.entity.Notification;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    @Mappings({
            @Mapping(target = "actorId", source = "actor.id"),
            @Mapping(target = "actorFirstName", source = "actor.firstName"),
            @Mapping(target = "actorLastName", source = "actor.lastName"),
            @Mapping(target = "actorAvatar", source = "actor.avatar"),
            @Mapping(target = "type", expression = "java(notification.getType().name())")
    })
    NotificationResponseDto toDto(Notification notification);
}
