package org.hit.chiikaiwabe.domain.entity;

import org.hit.chiikaiwabe.domain.entity.common.DateAuditing;
import org.hit.chiikaiwabe.domain.enums.ParticipantStatus;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "booking_participants",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_PARTICIPANT_BOOKING_USER",
                columnNames = {"booking_id", "user_id"}))
public class BookingParticipant extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(insertable = false, updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_PARTICIPANT_BOOKING"))
    private OfflineBooking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_PARTICIPANT_USER"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipantStatus status;

    @Column(name = "reminder_minutes_before")
    private Integer reminderMinutesBefore;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

}
