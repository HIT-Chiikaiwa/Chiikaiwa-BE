package org.hit.chiikaiwabe.domain.dto.response;

import java.time.LocalDateTime;
import org.hit.chiikaiwabe.domain.entity.AdminActionLog;
import org.hit.chiikaiwabe.domain.enums.AdminAction;
import org.hit.chiikaiwabe.domain.enums.UserStatus;

public record AdminActionLogDto(String id, String actorId, String targetId, AdminAction action,
                                UserStatus previousStatus, UserStatus newStatus,
                                LocalDateTime createdDate, String reason, String reportId) {
    public static AdminActionLogDto from(AdminActionLog log) {
        return new AdminActionLogDto(log.getId(), log.getActor().getId(), log.getTarget().getId(),
                log.getAction(), log.getPreviousStatus(), log.getNewStatus(), log.getCreatedDate(),
                log.getReason(), log.getReport() == null ? null : log.getReport().getId());
    }
}
