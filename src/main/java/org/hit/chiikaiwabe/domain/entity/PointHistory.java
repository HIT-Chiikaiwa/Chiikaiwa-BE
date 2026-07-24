package org.hit.chiikaiwabe.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hit.chiikaiwabe.domain.entity.common.DateAuditing;
import org.hit.chiikaiwabe.domain.enums.PointAction;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "point_history", indexes = {
        @Index(name = "IDX_POINT_USER_DATE", columnList = "user_id, createdDate"),
        @Index(name = "IDX_POINT_USER_ACTION_DATE", columnList = "user_id, action, createdDate")
})
public class PointHistory extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(insertable = false, updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_POINT_USER"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PointAction action;

    @Column(nullable = false)
    private Integer points;

    @Column(name = "exp_after", nullable = false)
    private Long expAfter;

    @Column(name = "reference_id", columnDefinition = "CHAR(36)")
    private String referenceId;

}
