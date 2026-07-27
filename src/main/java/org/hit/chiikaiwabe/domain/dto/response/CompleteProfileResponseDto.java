package org.hit.chiikaiwabe.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompleteProfileResponseDto {
    private String tokenType = "Bearer";
    private String accessToken;
    private String refreshToken;
    private String userId;
    private boolean profileComplete = true;
}
