package org.hit.chiikaiwabe.domain.dto.response;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BlockedUserResponseDto {
    private String userId;
    private String firstName;
    private String lastName;
    private String avatar;
    private LocalDateTime blockedAt;
}
