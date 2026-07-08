package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.FriendshipResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.UserSearchResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FriendshipService {

    CommonResponseDto sendFriendRequest(String userId, String targetUserId);

    CommonResponseDto acceptFriendRequest(String userId, String requestId);

    CommonResponseDto rejectFriendRequest(String userId, String requestId);

    CommonResponseDto unfriend(String userId, String friendId);

    Page<FriendshipResponseDto> getFriends(String userId, Pageable pageable);

    Page<FriendshipResponseDto> getPendingRequests(String userId, Pageable pageable);

    Page<FriendshipResponseDto> searchFriendsByName(String userId, String keyword, Pageable pageable);

    UserSearchResponseDto searchUserByPhone(String userId, String phone);

}
