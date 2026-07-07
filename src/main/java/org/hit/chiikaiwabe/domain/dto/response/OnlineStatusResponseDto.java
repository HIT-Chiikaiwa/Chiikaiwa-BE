package org.hit.chiikaiwabe.domain.dto.response;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OnlineStatusResponseDto {
    private String userId;
    private Boolean isOnline;
    private LocalDateTime lastSeen;
}
