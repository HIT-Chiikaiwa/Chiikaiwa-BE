package org.hit.chiikaiwabe.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ScheduleInviteRequestDto {
    @NotBlank(message = "Payload is required")
    private String payload;
}
