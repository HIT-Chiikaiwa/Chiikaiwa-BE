package org.hit.chiikaiwabe.domain.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReplyMessageDto {
    private String id;
    private String senderName;
    private String content;
    private String messageType;
}
