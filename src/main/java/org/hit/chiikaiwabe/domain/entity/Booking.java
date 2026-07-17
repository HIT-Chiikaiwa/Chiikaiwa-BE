package org.hit.chiikaiwabe.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hit.chiikaiwabe.domain.entity.common.DateAuditing;
import org.hit.chiikaiwabe.domain.enums.BookingStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "offline_bookings", indexes = {
        @Index(name = "idx_booking_creator", columnList = "creator_id"),
        @Index(name = "idx_booking_status", columnList = "status"),
        @Index(name = "idx_booking_scheduled_at", columnList = "scheduled_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends DateAuditing {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @Column(name = "message_id", length = 36)
    private String messageId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private BookingStatus status;

    @Column(length = 255)
    private String subject;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    private Integer durationMinutes;

    @Column(length = 255, nullable = false)
    private String locationName;

    @Column(length = 255, nullable = false)
    private String locationAddress;

    @Column(length = 255, nullable = false)
    private String locationDistrict;

    @Column(length = 255, nullable = false)
    private String locationCity;

    @Column(length = 1000)
    private String note;

    @Column(nullable = false)
    private Boolean isRecurring = false;

    @Column(length = 36)
    private String cancelledBy;

    @Column(columnDefinition = "text")
    private String cancelReason;

    private Integer reminderMinutesBefore;
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingParticipant> participants = new ArrayList<>();
}
