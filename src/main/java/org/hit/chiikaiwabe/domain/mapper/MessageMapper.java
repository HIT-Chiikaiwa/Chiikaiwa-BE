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
            @Mapping(target = "senderName", expression = "java(message.getSender() != null ? message.getSender().getLastName() + \" \" + message.getSender().getFirstName() : \"System\")"),
            @Mapping(source = "sender.avatar", target = "senderAvatar"),
            @Mapping(source = "conversation.id", target = "conversationId"),
            @Mapping(target = "messageType", expression = "java(message.getMessageType() != null ? message.getMessageType().name() : null)"),
            @Mapping(target = "replyToMessage", ignore = true),
            @Mapping(target = "forwardedFrom", ignore = true),
            @Mapping(target = "reactions", ignore = true),
            @Mapping(target = "attachments", ignore = true),
            @Mapping(source = "isPinned", target = "isPinned")
    })
    MessageResponseDto toDto(Message message);
}
