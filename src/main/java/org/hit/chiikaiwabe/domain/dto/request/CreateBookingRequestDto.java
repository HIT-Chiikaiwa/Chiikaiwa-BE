package org.hit.chiikaiwabe.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequestDto {

    @NotNull
    private LocalDateTime scheduledAt;

    @NotBlank
    private String locationName;

    @NotBlank
    private String locationAddress;

    @NotBlank
    private String locationDistrict;

    @NotBlank
    private String locationCity;

    private String subject;

    private String note;

    private Boolean isRecurring;

    private Integer durationMinutes;

    private Integer reminderMinutesBefore;

}
