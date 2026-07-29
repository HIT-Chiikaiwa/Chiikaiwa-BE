package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.FriendshipResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.UserSearchResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FriendshipService {

    void sendFriendRequest(String senderId, String receiverId);

    void acceptFriendRequest(String userId, String requestId);

    void rejectFriendRequest(String userId, String requestId);

    void unfriend(String userId, String friendId);

    Page<FriendshipResponseDto> getFriends(String userId, Pageable pageable);

    Page<FriendshipResponseDto> getPendingRequests(String userId, Pageable pageable);

    Page<FriendshipResponseDto> searchFriendsByName(String userId, String keyword, Pageable pageable);

    UserSearchResponseDto searchUser(String userId, String keyword);

}
