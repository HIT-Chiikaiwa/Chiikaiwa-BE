package org.hit.chiikaiwabe.domain.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDto {

    private String id;
    private String status;
    private String subject;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;

    private String locationName;
    private String locationAddress;
    private String locationDistrict;
    private String locationCity;

    private String note;
    private Boolean isRecurring;
    private String cancelReason;

    private String creatorId;
    private String creatorName;
    private String creatorAvatar;

    private String partnerId;
    private String partnerName;
    private String partnerAvatar;

    private String participantStatus;
    private Boolean hasRated;
    private Integer myRating;

    private String messageId;
    private String conversationId;
    private LocalDateTime createdDate;

}
