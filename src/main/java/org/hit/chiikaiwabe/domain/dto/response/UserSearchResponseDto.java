package org.hit.chiikaiwabe.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserSearchResponseDto {

    private String id;

    private String firstName;

    private String lastName;

    private String avatar;

    private String phone;

    private String friendshipStatus;

}
