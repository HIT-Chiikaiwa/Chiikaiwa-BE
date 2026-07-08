package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.FriendshipResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.UserSearchResponseDto;
import org.hit.chiikaiwabe.domain.entity.Friendship;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.FriendshipStatus;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.repository.FriendshipRepository;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.service.BlockReportService;
import org.hit.chiikaiwabe.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final BlockReportService blockReportService;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public CommonResponseDto sendFriendRequest(String userId, String targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new InvalidException(ErrorMessage.Friendship.ERR_SELF_REQUEST);
        }

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId}));
        User receiver = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{targetUserId}));

        if (blockReportService.isBlocked(userId, targetUserId)) {
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
        return new CommonResponseDto(true, SuccessMessage.Friendship.REQUEST_SENT);
    }

    @Override
    @Transactional
    public CommonResponseDto acceptFriendRequest(String userId, String requestId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Friendship.ERR_REQUEST_NOT_FOUND));

        if (!friendship.getReceiver().getId().equals(userId)) {
            throw new InvalidException(ErrorMessage.Friendship.ERR_NOT_RECEIVER);
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);

        User accepter = friendship.getReceiver();
        messagingTemplate.convertAndSendToUser(
                friendship.getRequester().getId(),
                "/queue/friendship",
                Map.of(
                        "type", "FRIEND_REQUEST_ACCEPTED",
                        "userId", accepter.getId(),
                        "firstName", accepter.getFirstName(),
                        "lastName", accepter.getLastName(),
                        "avatar", accepter.getAvatar() != null ? accepter.getAvatar() : ""
                )
        );

        log.info("Friend request {} accepted by {}", requestId, userId);
        return new CommonResponseDto(true, SuccessMessage.Friendship.REQUEST_ACCEPTED);
    }

    @Override
    @Transactional
    public CommonResponseDto rejectFriendRequest(String userId, String requestId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Friendship.ERR_REQUEST_NOT_FOUND));

        if (!friendship.getReceiver().getId().equals(userId)) {
            throw new InvalidException(ErrorMessage.Friendship.ERR_NOT_RECEIVER);
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        friendshipRepository.save(friendship);

        log.info("Friend request {} rejected by {}", requestId, userId);
        return new CommonResponseDto(true, SuccessMessage.Friendship.REQUEST_REJECTED);
    }

    @Override
    @Transactional
    public CommonResponseDto unfriend(String userId, String friendId) {
        Friendship friendship = friendshipRepository.findFriendshipBetween(userId, friendId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Friendship.ERR_NOT_FRIENDS));

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new InvalidException(ErrorMessage.Friendship.ERR_NOT_FRIENDS);
        }

        friendshipRepository.delete(friendship);

        log.info("User {} unfriended {}", userId, friendId);
        return new CommonResponseDto(true, SuccessMessage.Friendship.UNFRIENDED);
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
        Page<Friendship> friends = friendshipRepository.searchFriendsByName(userId, keyword, pageable);
        return friends.map(f -> mapToFriendshipResponse(f, userId));
    }

    @Override
    public UserSearchResponseDto searchUserByPhone(String userId, String phone) {
        User foundUser = userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Friendship.ERR_USER_NOT_FOUND_PHONE));

        // Xác định trạng thái quan hệ
        String friendshipStatus = "STRANGER";
        Optional<Friendship> friendship = friendshipRepository.findFriendshipBetween(userId, foundUser.getId());
        if (friendship.isPresent()) {
            Friendship f = friendship.get();
            switch (f.getStatus()) {
                case ACCEPTED:
                    friendshipStatus = "FRIEND";
                    break;
                case PENDING:
                    friendshipStatus = f.getRequester().getId().equals(userId)
                            ? "PENDING_SENT" : "PENDING_RECEIVED";
                    break;
                default:
                    friendshipStatus = "STRANGER";
            }
        }

        return UserSearchResponseDto.builder()
                .id(foundUser.getId())
                .firstName(foundUser.getFirstName())
                .lastName(foundUser.getLastName())
                .avatar(foundUser.getAvatar())
                .phone(foundUser.getPhone())
                .friendshipStatus(friendshipStatus)
                .build();
    }

    // ========================= PRIVATE HELPERS =========================

    private FriendshipResponseDto mapToFriendshipResponse(Friendship f, String currentUserId) {
        // Xác định "người kia" (không phải mình)
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
        messagingTemplate.convertAndSendToUser(
                receiverUserId,
                "/queue/friendship",
                Map.of(
                        "type", "FRIEND_REQUEST_RECEIVED",
                        "userId", requester.getId(),
                        "firstName", requester.getFirstName(),
                        "lastName", requester.getLastName(),
                        "avatar", requester.getAvatar() != null ? requester.getAvatar() : ""
                )
        );
    }

}
