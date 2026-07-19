package org.hit.chiikaiwabe.domain.entity;

import org.hit.chiikaiwabe.domain.entity.common.DateAuditing;
import org.hit.chiikaiwabe.domain.enums.ReportReason;
import org.hit.chiikaiwabe.domain.enums.ReportStatus;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import jakarta.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "user_reports")
public class UserReport extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(insertable = false, updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_REPORT_REPORTER"))
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_REPORT_REPORTED"))
    private User reported;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id",
            foreignKey = @ForeignKey(name = "FK_REPORT_CONVERSATION"))
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id",
            foreignKey = @ForeignKey(name = "FK_REPORT_MESSAGE"))
    private Message message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

}
