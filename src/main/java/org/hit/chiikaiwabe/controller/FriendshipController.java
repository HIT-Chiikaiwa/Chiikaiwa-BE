package org.hit.chiikaiwabe.controller;

import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.base.RestData;
import org.hit.chiikaiwabe.constant.UrlConstant;
import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.FriendshipResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.UserSearchResponseDto;
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

@RequiredArgsConstructor
@RestApiV1
public class FriendshipController {

    private final FriendshipService friendshipService;

    @Tag(name = "friendship-controller")
    @Operation(summary = "Send friend request")
    @PostMapping(UrlConstant.Friendship.SEND_REQUEST)
    public ResponseEntity<RestData<CommonResponseDto>> sendFriendRequest(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PathVariable String targetUserId) {
        return VsResponseUtil.success(HttpStatus.CREATED,
                friendshipService.sendFriendRequest(principal.getId(), targetUserId));
    }

    @Tag(name = "friendship-controller")
    @Operation(summary = "Accept friend request")
    @PutMapping(UrlConstant.Friendship.ACCEPT_REQUEST)
    public ResponseEntity<RestData<CommonResponseDto>> acceptFriendRequest(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PathVariable String requestId) {
        return VsResponseUtil.success(
                friendshipService.acceptFriendRequest(principal.getId(), requestId));
    }

    @Tag(name = "friendship-controller")
    @Operation(summary = "Reject friend request")
    @PutMapping(UrlConstant.Friendship.REJECT_REQUEST)
    public ResponseEntity<RestData<CommonResponseDto>> rejectFriendRequest(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PathVariable String requestId) {
        return VsResponseUtil.success(
                friendshipService.rejectFriendRequest(principal.getId(), requestId));
    }

    @Tag(name = "friendship-controller")
    @Operation(summary = "Unfriend")
    @DeleteMapping(UrlConstant.Friendship.UNFRIEND)
    public ResponseEntity<RestData<CommonResponseDto>> unfriend(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PathVariable String friendId) {
        return VsResponseUtil.success(
                friendshipService.unfriend(principal.getId(), friendId));
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
    @Operation(summary = "Search user by phone number (all users in system)")
    @GetMapping(UrlConstant.UserSearch.SEARCH_BY_PHONE)
    public ResponseEntity<RestData<UserSearchResponseDto>> searchUserByPhone(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @RequestParam String phone) {
        return VsResponseUtil.success(
                friendshipService.searchUserByPhone(principal.getId(), phone));
    }

}
