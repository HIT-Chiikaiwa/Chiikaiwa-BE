package org.hit.chiikaiwabe.domain.mapper;

import org.hit.chiikaiwabe.domain.dto.response.MessageResponseDto;
import org.hit.chiikaiwabe.domain.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mappings({
            @Mapping(source = "sender.id", target = "senderId"),
            @Mapping(target = "senderName", expression = "java(message.getSender() != null ? message.getSender().getFirstName() + \" \" + message.getSender().getLastName() : null)"),
            @Mapping(source = "sender.avatar", target = "senderAvatar"),
            @Mapping(source = "conversation.id", target = "conversationId")
    })
    MessageResponseDto toDto(Message message);
}
