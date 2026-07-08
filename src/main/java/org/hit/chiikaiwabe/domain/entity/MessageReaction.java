package org.hit.chiikaiwabe.domain.entity;

import org.hit.chiikaiwabe.domain.entity.common.DateAuditing;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "message_reactions",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_REACTION_MESSAGE_USER",
                columnNames = {"message_id", "user_id"}))
public class MessageReaction extends DateAuditing {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(insertable = false, updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_REACTION_MESSAGE"))
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_REACTION_USER"))
    private User user;

    @Column(nullable = false, length = 10)
    private String emoji;

}
