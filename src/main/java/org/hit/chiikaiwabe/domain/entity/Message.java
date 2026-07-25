package org.hit.chiikaiwabe.domain.entity;

import org.hit.chiikaiwabe.domain.entity.common.DateAuditing;
import org.hit.chiikaiwabe.domain.enums.MessageType;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import jakarta.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "messages",
        indexes = {
                @Index(name = "IDX_MESSAGE_CONVERSATION_DATE", columnList = "conversation_id, created_date DESC, sender_id")
        })
public class Message extends DateAuditing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(insertable = false, updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_MESSAGE_CONVERSATION"))
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id",
            foreignKey = @ForeignKey(name = "FK_MESSAGE_SENDER"))
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType;

    @Column(name = "is_recalled", nullable = false)
    @Builder.Default
    private Boolean isRecalled = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id",
            foreignKey = @ForeignKey(name = "FK_MESSAGE_REPLY_TO"))
    private Message replyToMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "forwarded_from_message_id",
            foreignKey = @ForeignKey(name = "FK_MESSAGE_FORWARDED_FROM"))
    private Message forwardedFromMessage;

    @Column(name = "is_pinned", columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean isPinned = Boolean.FALSE;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<MessageAttachment> attachments = new java.util.ArrayList<>();

    public void addAttachment(MessageAttachment attachment) {
        attachments.add(attachment);
        attachment.setMessage(this);
    }

}
