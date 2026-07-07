package org.hit.chiikaiwabe.domain.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateDirectConversationRequestDto {
    @NotBlank private String targetUserId;
}
