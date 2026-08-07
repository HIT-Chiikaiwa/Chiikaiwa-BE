package org.hit.chiikaiwabe.domain.mapper;

import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;
import org.hit.chiikaiwabe.domain.entity.BookingParticipant;
import org.hit.chiikaiwabe.domain.entity.OfflineBooking;
import org.hit.chiikaiwabe.domain.entity.User;
import org.mapstruct.*;
import java.util.List;

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
    BookingResponseDto toDto(OfflineBooking booking, @Context String currentUserId);

    List<BookingResponseDto> toDtoList(List<OfflineBooking> bookings, @Context String currentUserId);

    @Mapping(source = "user.id", target = "userId")
    BookingResponseDto.ParticipantDto participantToParticipantDto(BookingParticipant participant);

    @AfterMapping
    default void customizeDynamicFields(@MappingTarget BookingResponseDto dto, OfflineBooking booking, @Context String currentUserId) {

        User creator = booking.getCreator();
        if (creator != null) {
            dto.setCreatorId(creator.getId());
            dto.setCreatorName(creator.getLastName() + " " + creator.getFirstName());
            dto.setCreatorAvatar(creator.getAvatar());
        }

        boolean isCreator = creator != null && creator.getId().equals(currentUserId);

        BookingParticipant me = null;
        BookingParticipant partner = null;

        if (booking.getParticipants() != null) {
            for (BookingParticipant p : booking.getParticipants()) {
                if (p.getUser() != null) {
                    if (p.getUser().getId().equals(currentUserId)) {
                        me = p;
                    } else {
                        partner = p;
                    }
                }
            }
        }

        if (isCreator) {
            dto.setParticipantStatus("CREATOR");
            if (partner != null) {
                User partnerUser = partner.getUser();
                dto.setPartnerId(partnerUser.getId());
                dto.setPartnerName(partnerUser.getLastName() + " " + partnerUser.getFirstName());
                dto.setPartnerAvatar(partnerUser.getAvatar());
            }
        } else {
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
