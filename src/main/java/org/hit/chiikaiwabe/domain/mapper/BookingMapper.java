package org.hit.chiikaiwabe.domain.mapper;

import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;
import org.hit.chiikaiwabe.domain.entity.BookingParticipant;
import org.hit.chiikaiwabe.domain.entity.OfflineBooking;
import org.hit.chiikaiwabe.domain.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mappings({
            @Mapping(target = "status", expression = "java(booking.getStatus() != null ? booking.getStatus().name() : null)"),
            @Mapping(source = "conversation.id", target = "conversationId"),
            @Mapping(target = "creatorId", ignore = true),
            @Mapping(target = "creatorName", ignore = true),
            @Mapping(target = "creatorAvatar", ignore = true),
            @Mapping(target = "partnerId", ignore = true),
            @Mapping(target = "partnerName", ignore = true),
            @Mapping(target = "partnerAvatar", ignore = true),
            @Mapping(target = "participantStatus", ignore = true),
            @Mapping(target = "hasRated", ignore = true),
            @Mapping(target = "myRating", ignore = true)
    })
    BookingResponseDto toDto(OfflineBooking booking, @Context String currentUserId, @Context boolean hasRated, @Context Integer myRating);

    @AfterMapping
    default void customizeDynamicFields(@MappingTarget BookingResponseDto dto, OfflineBooking booking, @Context String currentUserId, @Context boolean hasRated, @Context Integer myRating) {
        dto.setHasRated(hasRated);
        dto.setMyRating(myRating);

        User creator = booking.getCreator();
        if (creator != null) {
            dto.setCreatorId(creator.getId());
            dto.setCreatorName(creator.getLastName() + " " + creator.getFirstName());
            dto.setCreatorAvatar(creator.getAvatar());
        }

        boolean isCreator = creator != null && creator.getId().equals(currentUserId);

        if (isCreator) {
            dto.setParticipantStatus("CREATOR");
            BookingParticipant partner = booking.getParticipants().stream()
                    .filter(p -> !p.getUser().getId().equals(currentUserId))
                    .findFirst()
                    .orElse(null);

            if (partner != null) {
                User partnerUser = partner.getUser();
                dto.setPartnerId(partnerUser.getId());
                dto.setPartnerName(partnerUser.getLastName() + " " + partnerUser.getFirstName());
                dto.setPartnerAvatar(partnerUser.getAvatar());
            }
        } else {
            BookingParticipant me = booking.getParticipants().stream()
                    .filter(p -> p.getUser().getId().equals(currentUserId))
                    .findFirst()
                    .orElse(null);

            if (me != null) {
                dto.setParticipantStatus(me.getStatus().name());
            } else {
                dto.setParticipantStatus("NOT_INVOLVED");
            }

            if (creator != null) {
                dto.setPartnerId(creator.getId());
                dto.setPartnerName(creator.getLastName() + " " + creator.getFirstName());
                dto.setPartnerAvatar(creator.getAvatar());
            }
        }
    }
}
