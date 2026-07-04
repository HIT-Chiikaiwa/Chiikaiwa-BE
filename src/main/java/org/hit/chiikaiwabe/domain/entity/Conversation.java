package org.hit.chiikaiwabe.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hit.chiikaiwabe.domain.entity.common.DateAuditing;
import org.hit.chiikaiwabe.domain.enums.ConversationType;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "conversations")
public class Conversation extends DateAuditing {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(insertable = false, updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationType type;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "group_avatar")
    private String groupAvatar;

    @Column(name = "max_members", nullable = false)
    @Builder.Default
    private Integer maxMembers = 30;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User createdBy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_id", referencedColumnName = "id")
    private Message lastMessage;
}
