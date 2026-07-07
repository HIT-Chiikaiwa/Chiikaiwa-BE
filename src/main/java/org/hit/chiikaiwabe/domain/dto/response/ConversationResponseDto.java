package org.hit.chiikaiwabe.domain.dto.response;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConversationResponseDto {
    private String id;
    private String type;
    private String groupName;
    private String groupAvatar;
    private Integer memberCount;
    private MessageResponseDto lastMessage;
    private Integer unreadCount;
    private Boolean hasLeft;
}
