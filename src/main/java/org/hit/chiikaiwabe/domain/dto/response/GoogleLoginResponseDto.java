package org.hit.chiikaiwabe.domain.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoogleLoginResponseDto {
    private String email;
    private String userId;
    private boolean profileComplete;

    private String tokenType;
    private String accessToken;
    private String refreshToken;

    public GoogleLoginResponseDto(String email, String userId) {
        this.email = email;
        this.userId = userId;
        this.profileComplete = false;
    }

    public GoogleLoginResponseDto(String email, String userId,
                                  String accessToken, String refreshToken) {
        this.email = email;
        this.userId = userId;
        this.profileComplete = true;
        this.tokenType = "Bearer";
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
