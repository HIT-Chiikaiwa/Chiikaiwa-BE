package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.entity.UserBlock;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.repository.UserBlockRepository;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.service.UserBlockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hit.chiikaiwabe.domain.dto.response.BlockedUserDto;

@Service
@Transactional
public class UserBlockServiceImpl implements UserBlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

    public UserBlockServiceImpl(UserBlockRepository userBlockRepository, UserRepository userRepository) {
        this.userBlockRepository = userBlockRepository;
        this.userRepository = userRepository;
    }

    public void blockUser(String blockerId, String blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException(ErrorMessage.Chat.ERR_CANNOT_BLOCK_YOURSELF);
        }

        if (userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            return;
        }

        User blocker = userRepository.findById(blockerId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));
        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));

        UserBlock userBlock = UserBlock.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();
        userBlockRepository.save(userBlock);
    }

    public void unblockUser(String blockerId, String blockedId) {
        Optional<UserBlock> blockOpt = userBlockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId);
        blockOpt.ifPresent(userBlockRepository::delete);
    }

    public List<BlockedUserDto> getBlockedUsers(String blockerId) {
        return userBlockRepository.findByBlockerId(blockerId).stream()
                .map(block -> new BlockedUserDto(
                        block.getBlocked().getId(),
                        block.getBlocked().getFirstName(),
                        block.getBlocked().getLastName(),
                        block.getBlocked().getAvatar()
                )).collect(Collectors.toList());
    }

    public boolean isBlocked(String userA, String userB) {
        return userBlockRepository.existsByBlockerIdAndBlockedId(userA, userB) ||
               userBlockRepository.existsByBlockerIdAndBlockedId(userB, userA);
    }
}
