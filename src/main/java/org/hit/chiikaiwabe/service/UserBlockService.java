package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.response.BlockedUserDto;
import java.util.List;

public interface UserBlockService {
    void blockUser(String blockerId, String blockedId);
    void unblockUser(String blockerId, String blockedId);
    List<BlockedUserDto> getBlockedUsers(String blockerId);
    boolean isBlocked(String userA, String userB);
}
