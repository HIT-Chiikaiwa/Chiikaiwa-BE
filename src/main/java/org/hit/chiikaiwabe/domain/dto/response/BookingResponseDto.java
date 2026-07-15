package org.hit.chiikaiwabe.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hit.chiikaiwabe.domain.enums.BookingStatus;
import org.hit.chiikaiwabe.domain.enums.ParticipantStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponseDto {
    private String id;
    private String creatorId;
    private String conversationId;
    private String messageId;
    private BookingStatus status;
    private String subject;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private String locationName;
    private String locationAddress;
    private String locationDistrict;
    private String locationCity;
    private String note;
    private Boolean isRecurring;
    private String cancelledBy;
    private String cancelReason;
    private Integer reminderMinutesBefore;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private List<ParticipantDto> participants;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ParticipantDto {
        private String id;
        private String userId;
        private ParticipantStatus status;
        private Integer reminderMinutesBefore;
        private LocalDateTime respondedAt;
    }
}
