package org.hit.chiikaiwabe.domain.dto.request;
import org.hit.chiikaiwabe.domain.enums.ReportReason;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReportUserRequestDto {
    @NotBlank private String reportedId;
    private String conversationId;
    private String messageId;
    private ReportReason reason;
    private String description;
}
