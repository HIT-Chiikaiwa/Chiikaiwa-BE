package org.hit.chiikaiwabe.controller;

import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.security.CurrentUser;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.UserBlockService;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.hit.chiikaiwabe.domain.dto.response.BlockedUserDto;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestApiV1
public class UserBlockController {

    private final UserBlockService userBlockService;

    @PostMapping("/users/block/{userId}")
    public ResponseEntity<?> blockUser(@PathVariable String userId,
                                       @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        userBlockService.blockUser(principal.getId(), userId);
        return VsResponseUtil.success(SuccessMessage.Chat.USER_BLOCKED_SUCCESS);
    }

    @DeleteMapping("/users/block/{userId}")
    public ResponseEntity<?> unblockUser(@PathVariable String userId,
                                         @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        userBlockService.unblockUser(principal.getId(), userId);
        return VsResponseUtil.success(SuccessMessage.Chat.USER_UNBLOCKED_SUCCESS);
    }

    @GetMapping("/users/blocked")
    public ResponseEntity<?> getBlockedUsers(@Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        var blockedUsers = userBlockService.getBlockedUsers(principal.getId()).stream()
                .map(block -> new BlockedUserDto(
                        block.getBlocked().getId(),
                        block.getBlocked().getFirstName(),
                        block.getBlocked().getLastName(),
                        block.getBlocked().getAvatar()
                )).collect(Collectors.toList());
        return VsResponseUtil.success(blockedUsers);
    }
}
