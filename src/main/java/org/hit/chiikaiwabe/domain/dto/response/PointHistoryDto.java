package org.hit.chiikaiwabe.domain.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PointHistoryDto {
    private String id;
    private String action;
    private String actionDescription;
    private Integer points;
    private Long expAfter;
    private LocalDateTime createdDate;
}
