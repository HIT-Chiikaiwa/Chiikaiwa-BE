package org.hit.chiikaiwabe.controller;

import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.base.RestData;
import org.hit.chiikaiwabe.constant.UrlConstant;
import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.FriendshipResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.UserSearchResponseDto;

import java.util.List;
import org.hit.chiikaiwabe.security.CurrentUser;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.FriendshipService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.hit.chiikaiwabe.annotation.RateLimit;

@RequiredArgsConstructor
@RestApiV1
@RateLimit(capacity = 30, durationInSeconds = 60)
public class FriendshipController {

    private final FriendshipService friendshipService;

    @Tag(name = "friendship-controller")
    @Operation(summary = "Send friend request")
    @RateLimit(capacity = 10, durationInSeconds = 60)
    @PostMapping(UrlConstant.Friendship.SEND_REQUEST)
    public ResponseEntity<RestData<CommonResponseDto>> sendFriendRequest(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PathVariable String targetUserId) {
        friendshipService.sendFriendRequest(principal.getId(), targetUserId);
        return VsResponseUtil.success(HttpStatus.CREATED, new CommonResponseDto(true, org.hit.chiikaiwabe.constant.SuccessMessage.Friendship.REQUEST_SENT));
    }

    @Tag(name = "friendship-controller")
    @Operation(summary = "Accept friend request")
    @PutMapping(UrlConstant.Friendship.ACCEPT_REQUEST)
    public ResponseEntity<RestData<CommonResponseDto>> acceptFriendRequest(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PathVariable String requestId) {
        friendshipService.acceptFriendRequest(principal.getId(), requestId);
        return VsResponseUtil.success(new CommonResponseDto(true, org.hit.chiikaiwabe.constant.SuccessMessage.Friendship.REQUEST_ACCEPTED));
    }

    @Tag(name = "friendship-controller")
    @Operation(summary = "Reject friend request")
    @PutMapping(UrlConstant.Friendship.REJECT_REQUEST)
    public ResponseEntity<RestData<CommonResponseDto>> rejectFriendRequest(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PathVariable String requestId) {
        friendshipService.rejectFriendRequest(principal.getId(), requestId);
        return VsResponseUtil.success(new CommonResponseDto(true, org.hit.chiikaiwabe.constant.SuccessMessage.Friendship.REQUEST_REJECTED));
    }

    @Tag(name = "friendship-controller")
    @Operation(summary = "Unfriend")
    @DeleteMapping(UrlConstant.Friendship.UNFRIEND)
    public ResponseEntity<RestData<CommonResponseDto>> unfriend(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PathVariable String friendId) {
        friendshipService.unfriend(principal.getId(), friendId);
        return VsResponseUtil.success(new CommonResponseDto(true, org.hit.chiikaiwabe.constant.SuccessMessage.Friendship.UNFRIENDED));
    }

    @Tag(name = "friendship-controller")
    @Operation(summary = "Get friends list (paginated)")
    @GetMapping(UrlConstant.Friendship.FRIENDS_LIST)
    public ResponseEntity<RestData<Page<FriendshipResponseDto>>> getFriends(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return VsResponseUtil.success(
                friendshipService.getFriends(principal.getId(), pageable));
    }

    @Tag(name = "friendship-controller")
    @Operation(summary = "Get pending friend requests")
    @GetMapping(UrlConstant.Friendship.PENDING_REQUESTS)
    public ResponseEntity<RestData<Page<FriendshipResponseDto>>> getPendingRequests(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return VsResponseUtil.success(
                friendshipService.getPendingRequests(principal.getId(), pageable));
    }

    @Tag(name = "friendship-controller")
    @Operation(summary = "Search friends by name (only within your friends list)")
    @GetMapping(UrlConstant.Friendship.SEARCH_FRIENDS)
    public ResponseEntity<RestData<Page<FriendshipResponseDto>>> searchFriendsByName(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return VsResponseUtil.success(
                friendshipService.searchFriendsByName(principal.getId(), keyword, pageable));
    }

    @Tag(name = "friendship-controller")
    @Operation(summary = "Search user by phone, email or name (all users in system)")
    @RateLimit(capacity = 10, durationInSeconds = 60)
    @GetMapping(UrlConstant.UserSearch.SEARCH)
    public ResponseEntity<RestData<List<UserSearchResponseDto>>> searchUser(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @RequestParam String keyword) {
        return VsResponseUtil.success(
                friendshipService.searchUser(principal.getId(), keyword));
    }

}
