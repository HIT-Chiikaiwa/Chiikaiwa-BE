package org.hit.chiikaiwabe.domain.dto.response;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportResponseDto {
    private String id;
    private String reportedId;
    private String reason;
    private String status;
    private LocalDateTime createdDate;
}
