package org.hit.chiikaiwabe.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NearbyUserDto {
    private String userId;
    private String firstName;
    private String lastName;
    private String avatar;
    private String university;
    private String majorName;
    private String statusTag;

    private Double latitude;
    private Double longitude;
    private Double distanceKm;
}
