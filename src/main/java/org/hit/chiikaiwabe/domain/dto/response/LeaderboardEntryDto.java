package org.hit.chiikaiwabe.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LeaderboardEntryDto {
    private long rank;
    private String userId;
    private String firstName;
    private String lastName;
    private String avatar;
    private Long expPoints;
    private String title;
    private String titleIcon;
}
