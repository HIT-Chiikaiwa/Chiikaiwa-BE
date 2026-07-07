package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.domain.dto.request.ReportUserRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.BlockedUserResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.ReportResponseDto;
import org.hit.chiikaiwabe.service.BlockReportService;
import org.hit.chiikaiwabe.service.UserBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BlockReportServiceImpl implements BlockReportService {

    // Delegate to the actual implementations created by DEV 2
    private final UserBlockService userBlockService;

    @Override
    public void blockUser(String blockerId, String blockedId) {
        userBlockService.blockUser(blockerId, blockedId);
    }

    @Override
    public void unblockUser(String blockerId, String blockedId) {
        userBlockService.unblockUser(blockerId, blockedId);
    }

    @Override
    public List<BlockedUserResponseDto> getBlockedUsers(String userId) {
        return null; // Not used - UserBlockController returns BlockedUserDto directly
    }

    @Override
    public boolean isBlocked(String userId1, String userId2) {
        return userBlockService.isBlocked(userId1, userId2);
    }

    @Override
    public ReportResponseDto reportUser(String reporterId, ReportUserRequestDto dto) {
        return null; // Not used - ReportController uses ReportService directly
    }
}
