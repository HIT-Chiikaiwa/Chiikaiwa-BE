package org.hit.chiikaiwabe.domain.entity;

import org.hit.chiikaiwabe.domain.entity.common.DateAuditing;
import org.hit.chiikaiwabe.domain.enums.NotificationType;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import jakarta.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "notifications",
        indexes = {
                @Index(name = "IDX_NOTIF_RECIPIENT_DATE", columnList = "recipient_id, created_date DESC")
        })
public class Notification extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(insertable = false, updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_NOTIFICATION_RECIPIENT"))
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_NOTIFICATION_ACTOR"))
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Nationalized
    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "target_id", length = 36)
    private String targetId;

    @Column(name = "target_type", length = 20)
    private String targetType;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;
}
