package org.hit.chiikaiwabe.domain.entity;

import org.hit.chiikaiwabe.domain.entity.common.DateAuditing;
import lombok.*;

import jakarta.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "booking_ratings",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_RATING_BOOKING_RATER",
                columnNames = {"booking_id", "rater_id"}))
public class BookingRating extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(insertable = false, updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_RATING_BOOKING"))
    private OfflineBooking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rater_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_RATING_RATER"))
    private User rater;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rated_user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_RATING_RATED_USER"))
    private User ratedUser;

    @Column(nullable = false)
    private Integer score;

}
