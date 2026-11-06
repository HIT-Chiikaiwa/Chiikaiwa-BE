package org.hit.chiikaiwabe.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hit.chiikaiwabe.domain.entity.common.DateAuditing;
import org.hit.chiikaiwabe.domain.enums.AdminAction;
import org.hit.chiikaiwabe.domain.enums.UserStatus;

@Entity
@Table(name = "admin_action_logs", indexes = @Index(name = "idx_admin_action_target_date", columnList = "target_id,created_date"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminActionLog extends DateAuditing {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false, updatable = false)
    private User actor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_id", nullable = false, updatable = false)
    private User target;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private AdminAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private UserStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private UserStatus newStatus;

    @Column(nullable = false, updatable = false, length = 1000)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", updatable = false)
    private UserReport report;
}
