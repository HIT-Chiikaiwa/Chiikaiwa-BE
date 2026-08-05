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
public class NotificationResponseDto {

    private String id;

    private String type;

    private String content;

    private String actorId;

    private String actorFirstName;

    private String actorLastName;

    private String actorAvatar;

    private String targetId;

    private String targetType;

    private Boolean isRead;

    private LocalDateTime createdDate;
}
