package org.hit.chiikaiwabe.domain.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hit.chiikaiwabe.constant.ErrorMessage;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GpsUpdateRequestDto {

    @NotNull(message = ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED)
    @Min(value = -90, message = ErrorMessage.Location.ERR_INVALID_COORDINATES)
    @Max(value = 90, message = ErrorMessage.Location.ERR_INVALID_COORDINATES)
    private Double latitude;

    @NotNull(message = ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED)
    @Min(value = -180, message = ErrorMessage.Location.ERR_INVALID_COORDINATES)
    @Max(value = 180, message = ErrorMessage.Location.ERR_INVALID_COORDINATES)
    private Double longitude;
}
