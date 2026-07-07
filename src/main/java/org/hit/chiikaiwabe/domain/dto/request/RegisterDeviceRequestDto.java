package org.hit.chiikaiwabe.domain.dto.request;
import org.hit.chiikaiwabe.domain.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegisterDeviceRequestDto {
    @NotBlank private String fcmToken;
    @NotNull private DeviceType deviceType;
    private String deviceName;
}
