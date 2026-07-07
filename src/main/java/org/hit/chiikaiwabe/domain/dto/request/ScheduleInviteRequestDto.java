package org.hit.chiikaiwabe.domain.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ScheduleInviteRequestDto {
    @NotBlank private String subject;
    private String location;
    @NotNull private LocalDateTime scheduledAt;
    private Integer duration;
    private String note;
}
