package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.pagination.PaginationResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.LeaderboardEntryDto;
import org.hit.chiikaiwabe.domain.dto.response.PointHistoryDto;
import org.hit.chiikaiwabe.domain.dto.response.UserRankDto;
import org.hit.chiikaiwabe.domain.enums.PointAction;

public interface LeaderboardService {

    void awardPoints(String userId, PointAction action, String referenceId);

    PaginationResponseDto<LeaderboardEntryDto> getLeaderboard(int page, int size);

    UserRankDto getUserRank(String userId);

    Long getUserRankNumber(String userId, Long expPoints);

    PaginationResponseDto<PointHistoryDto> getPointHistory(String userId, int page, int size);
}
