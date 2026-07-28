package org.hit.chiikaiwabe.domain.dto.request;
import jakarta.validation.constraints.NotBlank;
import org.hit.chiikaiwabe.domain.enums.MessageType;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SendMessageRequestDto {
    @NotBlank private String conversationId;
    @NotBlank private String content;
    private MessageType messageType;
}
