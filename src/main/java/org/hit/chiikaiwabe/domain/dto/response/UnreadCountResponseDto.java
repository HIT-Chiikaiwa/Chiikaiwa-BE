package org.hit.chiikaiwabe.domain.dto.response;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UnreadCountResponseDto {
    private String conversationId;
    private Integer count;
}
