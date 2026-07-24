package org.hit.chiikaiwabe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.RestData;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.constant.UrlConstant;
import org.hit.chiikaiwabe.domain.dto.pagination.PaginationResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.LeaderboardEntryDto;
import org.hit.chiikaiwabe.domain.dto.response.PointHistoryDto;
import org.hit.chiikaiwabe.domain.dto.response.UserRankDto;
import org.hit.chiikaiwabe.security.CurrentUser;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.LeaderboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestApiV1
@RequiredArgsConstructor
@Tag(name = "Leaderboard", description = "Leaderboards & EXP Points")
@SecurityRequirement(name = "bearerAuth")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping(UrlConstant.Leaderboard.TOP)
    @Operation(summary = "Lấy bảng xếp hạng top users")
    public ResponseEntity<RestData<PaginationResponseDto<LeaderboardEntryDto>>> getLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return VsResponseUtil.success(leaderboardService.getLeaderboard(page, size));
    }

    @GetMapping(UrlConstant.Leaderboard.MY_RANK)
    @Operation(summary = "Get my rank and EXP information")
    public ResponseEntity<RestData<UserRankDto>> getMyRank(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(leaderboardService.getUserRank(principal.getId()));
    }

    @GetMapping(UrlConstant.Leaderboard.HISTORY)
    @Operation(summary = "EXP point history (added/minus points)")
    public ResponseEntity<RestData<PaginationResponseDto<PointHistoryDto>>> getHistory(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return VsResponseUtil.success(
                leaderboardService.getPointHistory(principal.getId(), page, size));
    }
}
