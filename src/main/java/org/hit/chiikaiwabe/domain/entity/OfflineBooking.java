package org.hit.chiikaiwabe.domain.entity;

import org.hit.chiikaiwabe.domain.entity.common.DateAuditing;
import org.hit.chiikaiwabe.domain.enums.BookingStatus;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Nationalized;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "offline_bookings",
        indexes = {
                @Index(name = "IDX_BOOKING_CREATOR", columnList = "creator_id"),
                @Index(name = "IDX_BOOKING_STATUS", columnList = "status"),
                @Index(name = "IDX_BOOKING_SCHEDULED_AT", columnList = "scheduled_at")
        })
public class OfflineBooking extends DateAuditing {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(insertable = false, updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_BOOKING_CREATOR"))
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id",
            foreignKey = @ForeignKey(name = "FK_BOOKING_CONVERSATION"))
    private Conversation conversation;

    @Column(name = "message_id", columnDefinition = "CHAR(36)")
    private String messageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Nationalized
    @Column
    private String subject;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Nationalized
    @Column(name = "location_name", nullable = false)
    private String locationName;

    @Nationalized
    @Column(name = "location_address", nullable = false)
    private String locationAddress;

    @Nationalized
    @Column(name = "location_district", nullable = false)
    private String locationDistrict;

    @Nationalized
    @Column(name = "location_city", nullable = false)
    private String locationCity;

    @Nationalized
    @Column
    private String note;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = Boolean.FALSE;

    @Column(name = "cancelled_by", columnDefinition = "CHAR(36)")
    private String cancelledBy;

    @Nationalized
    @Column(name = "cancel_reason", columnDefinition = "NTEXT")
    private String cancelReason;

    @Column(name = "reminder_minutes_before")
    private Integer reminderMinutesBefore;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookingParticipant> participants = new ArrayList<>();

    @Version
    @Column(name = "version")
    @Builder.Default
    private Long version = 0L;

}
