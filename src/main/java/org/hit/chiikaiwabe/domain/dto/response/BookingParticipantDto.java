package org.hit.chiikaiwabe.domain.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingParticipantDto {

    private String userId;
    private String userName;
    private String userAvatar;
    private String status;
    private LocalDateTime respondedAt;

}
