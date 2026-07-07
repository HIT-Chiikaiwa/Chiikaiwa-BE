package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.domain.dto.request.ReportUserRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.BlockedUserResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.ReportResponseDto;
import org.hit.chiikaiwabe.service.BlockReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BlockReportServiceImpl implements BlockReportService {

    @Override
    public void blockUser(String blockerId, String blockedId) { /* TODO */ }

    @Override
    public void unblockUser(String blockerId, String blockedId) { /* TODO */ }

    @Override
    public List<BlockedUserResponseDto> getBlockedUsers(String userId) { return null; /* TODO */ }

    @Override
    public boolean isBlocked(String userId1, String userId2) { return false; /* TODO */ }

    @Override
    public ReportResponseDto reportUser(String reporterId, ReportUserRequestDto dto) { return null; /* TODO */ }
}
