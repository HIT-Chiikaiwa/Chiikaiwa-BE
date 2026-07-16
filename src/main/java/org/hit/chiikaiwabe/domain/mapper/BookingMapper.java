package org.hit.chiikaiwabe.domain.mapper;

import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.MessageResponseDto;
import org.hit.chiikaiwabe.domain.entity.BookingParticipant;
import org.hit.chiikaiwabe.domain.entity.Message;
import org.hit.chiikaiwabe.domain.entity.OfflineBooking;
import org.hit.chiikaiwabe.domain.entity.User;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.awt.print.Book;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface BookingMapper {

    @Mappings({
            // nguoi tao va cuoc hen
            @Mapping(source = "creator.id", target = "creatorId"),
            @Mapping(target = "creatorName", expression = "java(booking.getCreator() != null ? " +
                    "booking.getCreator().getLastName() + \" \" + booking.getCreator().getFirstName() : \"System\")"),
            @Mapping(source = "creator.avatar", target = "creatorAvatar"),
            @Mapping(source = "conversation.id", target = "conversationId"),
            @Mapping(target = "status", expression = "java(booking.getStatus() != null ? booking.getStatus().name() : null)"),

            // doi tuong hen
            @Mapping(target = "partnerId", expression = "java(getPartner(booking, currentUserId) != null ? " +
                    "getPartner(booking, currentUserId).getId() : null)"),
            @Mapping(target = "partnerName", expression = "java(getPartnerName(booking, currentUserId))"),
            @Mapping(target = "partnerAvatar", expression = "java(getPartner(booking, currentUserId) != null ? " +
                    "getPartner(booking, currentUserId).getAvatar() : null)"),

            // trang thai
            @Mapping(target = "participantStatus", expression = "java(getMyStatus(booking, currentUserId))")
    })
    BookingResponseDto toDto(OfflineBooking booking, @Context String currentUserId);
    List<BookingResponseDto> toDtoList(List<OfflineBooking> bookings, @Context String currentUserId);

    default User getPartner(OfflineBooking booking, String currentUserId){
        if(booking == null || booking.getParticipants() == null){
            return null;
        }
        for(BookingParticipant p : booking.getParticipants()){
            if(p.getUser() != null && !p.getUser().getId().equals(currentUserId)){
                return p.getUser();
            }
        }
        return null;
    }

    default String getPartnerName(OfflineBooking booking, String currentUserId){
        User partner = getPartner(booking, currentUserId);
        if(partner != null){
            return partner.getLastName() + " " + partner.getFirstName();
        }
        return null;
    }

    default String getMyStatus(OfflineBooking booking, String currentUserId){
        if(booking == null || booking.getParticipants() == null)
            return null;
        for(BookingParticipant p : booking.getParticipants()){
            if(p.getUser() != null && p.getUser().getId().equals(currentUserId)){
                return p.getStatus() != null ? p.getStatus().name() : null;
            }
        }
        return  null;
    }
}

