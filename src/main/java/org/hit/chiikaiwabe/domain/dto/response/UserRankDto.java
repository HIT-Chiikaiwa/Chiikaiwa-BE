package org.hit.chiikaiwabe.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserRankDto {
    private String userId;
    private String firstName;
    private String lastName;
    private String avatar;
    private Long expPoints;
    private String title;
    private String titleIcon;
    private long rank;
    private long totalUsers;
    private Long nextTitleMinExp;
    private String nextTitle;
    private String nextTitleIcon;
    private double progressPercent;
}
