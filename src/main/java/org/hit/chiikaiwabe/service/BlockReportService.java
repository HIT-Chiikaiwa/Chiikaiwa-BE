package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.request.ReportUserRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.BlockedUserResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.ReportResponseDto;
import java.util.List;

public interface BlockReportService {
    void blockUser(String blockerId, String blockedId);
    void unblockUser(String blockerId, String blockedId);
    List<BlockedUserResponseDto> getBlockedUsers(String userId);
    boolean isBlocked(String userId1, String userId2);
    ReportResponseDto reportUser(String reporterId, ReportUserRequestDto dto);
}
