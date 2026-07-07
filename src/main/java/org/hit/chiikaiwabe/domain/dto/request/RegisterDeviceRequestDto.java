package org.hit.chiikaiwabe.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.domain.enums.DeviceType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterDeviceRequestDto {
    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    private String fcmToken;

    @NotNull(message = ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED)
    DeviceType deviceType;

    @NotBlank(message = ErrorMessage.NOT_BLANK_FIELD)
    String deviceName;
}
