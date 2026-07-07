package org.hit.chiikaiwabe.domain.dto.response;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageResponseDto {
    private String id;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private String messageType;
    private Boolean isRecalled;
    private LocalDateTime createdDate;
    private List<FileAttachmentResponseDto> attachments;
}
