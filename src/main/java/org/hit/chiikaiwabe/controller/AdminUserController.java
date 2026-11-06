package org.hit.chiikaiwabe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.annotation.RateLimit;
import org.hit.chiikaiwabe.base.*;
import org.hit.chiikaiwabe.constant.*;
import org.hit.chiikaiwabe.domain.dto.request.AdminAccountActionRequestDto;
import org.hit.chiikaiwabe.domain.dto.request.AdminUserSearchRequestDto;
import org.hit.chiikaiwabe.service.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestApiV1
@RequiredArgsConstructor
@Tag(name = "admin-user-controller")
@PreAuthorize("hasAuthority('ADMIN')")
@RateLimit(capacity = 30, durationInSeconds = 60)
public class AdminUserController {
    private final UserService userService;

    @GetMapping(UrlConstant.Admin.USERS)
    @Operation(summary = "Search users (zero-based page, size 1..100)")
    public ResponseEntity<?> getUsers(@Valid @ParameterObject AdminUserSearchRequestDto request) {
        var result = userService.getCustomers(request.toPaginationRequest());
        result.getMeta().setPageNum(request.getPage());
        return VsResponseUtil.success(result);
    }

    @GetMapping(UrlConstant.Admin.USER)
    @Operation(summary = "Get user details")
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        return VsResponseUtil.success(userService.getUserById(userId));
    }

    @PatchMapping(UrlConstant.Admin.LOCK)
    @Operation(summary = "Lock account with reason and optional report ID")
    public ResponseEntity<?> lock(@PathVariable String userId, @Valid @RequestBody AdminAccountActionRequestDto request) {
        userService.lockUser(userId, request);
        return VsResponseUtil.success(SuccessMessage.USER_LOCKED);
    }

    @PatchMapping(UrlConstant.Admin.UNLOCK)
    @Operation(summary = "Unlock account with reason and optional report ID")
    public ResponseEntity<?> unlock(@PathVariable String userId, @Valid @RequestBody AdminAccountActionRequestDto request) {
        userService.unlockUser(userId, request);
        return VsResponseUtil.success(SuccessMessage.USER_UNLOCKED);
    }

    @GetMapping(UrlConstant.Admin.ACTIONS)
    @Operation(summary = "Get account administration history, newest first")
    public ResponseEntity<?> actions(@PathVariable String userId, @Valid @ParameterObject AdminUserSearchRequestDto request) {
        return VsResponseUtil.success(userService.getAdminActions(userId, request));
    }
}
