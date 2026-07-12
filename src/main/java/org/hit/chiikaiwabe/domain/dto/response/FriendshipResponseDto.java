package org.hit.chiikaiwabe.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FriendshipResponseDto {

    private String requestId;

    private String userId;

    private String firstName;

    private String lastName;

    private String avatar;

    private String phone;

    private String status;

    private LocalDateTime createdDate;

}
