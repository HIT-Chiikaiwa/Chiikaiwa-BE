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
            @Mapping(target = "attachments", expression = "java(mapAttachments(message.getAttachments()))"),
            @Mapping(source = "isPinned", target = "isPinned")
    })
    MessageResponseDto toDto(Message message);

    default java.util.List<org.hit.chiikaiwabe.domain.dto.response.FileAttachmentResponseDto> mapAttachments(java.util.List<org.hit.chiikaiwabe.domain.entity.MessageAttachment> attachments) {
        if (attachments == null) return new java.util.ArrayList<>();
        return attachments.stream().map(a -> org.hit.chiikaiwabe.domain.dto.response.FileAttachmentResponseDto.builder()
                .id(a.getId())
                .fileUrl(a.getFileUrl())
                .fileName(a.getFileName())
                .fileType(a.getFileType())
                .fileSize(a.getFileSize())
                .build()).collect(java.util.stream.Collectors.toList());
    }
}
