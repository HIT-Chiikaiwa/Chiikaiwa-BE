package org.hit.chiikaiwabe.domain.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RateBookingRequestDto {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer score;

}
