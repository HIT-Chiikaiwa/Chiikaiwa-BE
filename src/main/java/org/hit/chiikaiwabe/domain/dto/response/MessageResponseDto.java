package org.hit.chiikaiwabe.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hit.chiikaiwabe.domain.enums.MessageType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDto {
    private String id;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private MessageType messageType;
    private Boolean isRecalled;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
