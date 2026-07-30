package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.FriendshipResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.UserSearchResponseDto;
import org.hit.chiikaiwabe.domain.entity.Friendship;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.FriendshipStatus;
import org.hit.chiikaiwabe.domain.enums.PointAction;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.repository.FriendshipRepository;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.service.UserBlockService;
import org.hit.chiikaiwabe.service.FriendshipService;
import org.hit.chiikaiwabe.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserBlockService userBlockService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final LeaderboardService leaderboardService;

    private static final String FRIENDSHIP_CHANNEL = "friendship:notify";

    @Override
    @Transactional
    public void sendFriendRequest(String userId, String targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new InvalidException(ErrorMessage.Friendship.ERR_SELF_REQUEST);
        }

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId}));
        User receiver = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{targetUserId}));

        if (userBlockService.isBlocked(userId, targetUserId)) {
            throw new InvalidException(ErrorMessage.Chat.ERR_USER_BLOCKED);
        }

        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(userId, targetUserId);
        if (existing.isPresent()) {
            Friendship f = existing.get();
            if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new InvalidException(ErrorMessage.Friendship.ERR_ALREADY_FRIENDS);
            }
            if (f.getStatus() == FriendshipStatus.PENDING) {
                throw new InvalidException(ErrorMessage.Friendship.ERR_REQUEST_ALREADY_SENT);
            }
            f.setRequester(requester);
            f.setReceiver(receiver);
            f.setStatus(FriendshipStatus.PENDING);
            friendshipRepository.save(f);
        } else {
            Friendship friendship = Friendship.builder()
                    .requester(requester)
                    .receiver(receiver)
                    .status(FriendshipStatus.PENDING)
                    .build();
            friendshipRepository.save(friendship);
        }

        notifyFriendRequest(targetUserId, requester);

        log.info("Friend request sent from {} to {}", userId, targetUserId);
    }

    @Override
    @Transactional
    public void acceptFriendRequest(String userId, String requestId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Friendship.ERR_REQUEST_NOT_FOUND));

        if (!friendship.getReceiver().getId().equals(userId)) {
            throw new InvalidException(ErrorMessage.Friendship.ERR_NOT_RECEIVER);
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);

        User accepter = friendship.getReceiver();
        publishFriendshipEvent(friendship.getRequester().getId(), Map.of(
                "type", "FRIEND_REQUEST_ACCEPTED",
                "userId", accepter.getId(),
                "firstName", accepter.getFirstName(),
                "lastName", accepter.getLastName(),
                "avatar", accepter.getAvatar() != null ? accepter.getAvatar() : ""
        ));

        log.info("Friend request {} accepted by {}", requestId, userId);

        // Cộng EXP cho cả 2 bên
        leaderboardService.awardPoints(friendship.getRequester().getId(),
                PointAction.FRIENDSHIP_ACCEPTED, friendship.getId());
        leaderboardService.awardPoints(friendship.getReceiver().getId(),
                PointAction.FRIENDSHIP_ACCEPTED, friendship.getId());
    }

    @Override
    @Transactional
    public void rejectFriendRequest(String userId, String requestId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Friendship.ERR_REQUEST_NOT_FOUND));

        if (!friendship.getReceiver().getId().equals(userId)) {
            throw new InvalidException(ErrorMessage.Friendship.ERR_NOT_RECEIVER);
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        friendshipRepository.save(friendship);

        log.info("Friend request {} rejected by {}", requestId, userId);
    }

    @Override
    @Transactional
    public void unfriend(String userId, String friendId) {
        Friendship friendship = friendshipRepository.findFriendshipBetween(userId, friendId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Friendship.ERR_NOT_FRIENDS));

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new InvalidException(ErrorMessage.Friendship.ERR_NOT_FRIENDS);
        }

        friendshipRepository.delete(friendship);

        log.info("User {} unfriended {}", userId, friendId);
    }

    @Override
    public Page<FriendshipResponseDto> getFriends(String userId, Pageable pageable) {
        Page<Friendship> friendships = friendshipRepository.findAcceptedFriendsByUserId(userId, pageable);
        return friendships.map(f -> mapToFriendshipResponse(f, userId));
    }

    @Override
    public Page<FriendshipResponseDto> getPendingRequests(String userId, Pageable pageable) {
        Page<Friendship> pending = friendshipRepository.findPendingRequestsForUser(userId, pageable);
        return pending.map(f -> mapToFriendshipResponse(f, userId));
    }

    @Override
    public Page<FriendshipResponseDto> searchFriendsByName(String userId, String keyword, Pageable pageable) {
        String searchKeyword = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        Page<Friendship> friends = friendshipRepository.searchFriendsByName(userId, searchKeyword, pageable);
        return friends.map(f -> mapToFriendshipResponse(f, userId));
    }

    @Override
    public List<UserSearchResponseDto> searchUser(String userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new InvalidException(ErrorMessage.INVALID_SOME_THING_FIELD_IS_REQUIRED, new String[]{"keyword"});
        }
        keyword = keyword.trim();

        List<User> foundUsers = new ArrayList<>();

        // Thử tìm chính xác theo SĐT/email
        userRepository.findByPhoneOrEmail(keyword).ifPresent(foundUsers::add);

        // Nếu không tìm thấy theo SĐT/email → tìm theo tên
        if (foundUsers.isEmpty()) {
            String searchKeyword = "%" + keyword.toLowerCase() + "%";
            foundUsers = userRepository.searchByName(searchKeyword);
        }

        if (foundUsers.isEmpty()) {
            throw new NotFoundException(ErrorMessage.Friendship.ERR_USER_NOT_FOUND);
        }

        return foundUsers.stream()
                .filter(u -> !u.getId().equals(userId))
                .map(foundUser -> {
                    String friendshipStatus = determineFriendshipStatus(userId, foundUser.getId());
                    return UserSearchResponseDto.builder()
                            .id(foundUser.getId())
                            .firstName(foundUser.getFirstName())
                            .lastName(foundUser.getLastName())
                            .avatar(foundUser.getAvatar())
                            .phone(foundUser.getPhone())
                            .friendshipStatus(friendshipStatus)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String determineFriendshipStatus(String currentUserId, String targetUserId) {
        return friendshipRepository.findFriendshipBetween(currentUserId, targetUserId)
                .map(f -> {
                    switch (f.getStatus()) {
                        case ACCEPTED: return "FRIEND";
                        case PENDING: return f.getRequester().getId().equals(currentUserId)
                                ? "PENDING_SENT" : "PENDING_RECEIVED";
                        default: return "STRANGER";
                    }
                })
                .orElse("STRANGER");
    }


    private FriendshipResponseDto mapToFriendshipResponse(Friendship f, String currentUserId) {
        User otherUser = f.getRequester().getId().equals(currentUserId)
                ? f.getReceiver() : f.getRequester();

        return FriendshipResponseDto.builder()
                .requestId(f.getId())
                .userId(otherUser.getId())
                .firstName(otherUser.getFirstName())
                .lastName(otherUser.getLastName())
                .avatar(otherUser.getAvatar())
                .phone(otherUser.getPhone())
                .status(f.getStatus().name())
                .createdDate(f.getCreatedDate())
                .build();
    }

    private void notifyFriendRequest(String receiverUserId, User requester) {
        publishFriendshipEvent(receiverUserId, Map.of(
                "type", "FRIEND_REQUEST_RECEIVED",
                "userId", requester.getId(),
                "firstName", requester.getFirstName(),
                "lastName", requester.getLastName(),
                "avatar", requester.getAvatar() != null ? requester.getAvatar() : ""
        ));
    }

    private void publishFriendshipEvent(String receiverUserId, Map<String, String> payload) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("receiverUserId", receiverUserId);
            event.put("payload", payload);
            redisTemplate.convertAndSend(FRIENDSHIP_CHANNEL, objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("Failed to publish friendship event to user {}: {}", receiverUserId, e.getMessage(), e);
        }
    }

}
