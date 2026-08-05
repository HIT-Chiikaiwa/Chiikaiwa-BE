package org.hit.chiikaiwabe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.annotation.RateLimit;
import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.RestData;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.constant.UrlConstant;
import org.hit.chiikaiwabe.domain.dto.request.DeleteNotificationsRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.NotificationResponseDto;
import org.hit.chiikaiwabe.security.CurrentUser;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestApiV1
@RateLimit(capacity = 30, durationInSeconds = 60)
public class NotificationController {

    private final NotificationService notificationService;

    @Tag(name = "notification-controller")
    @Operation(summary = "Get notifications (paginated, newest first)")
    @GetMapping(UrlConstant.Notification.LIST)
    public ResponseEntity<RestData<Page<NotificationResponseDto>>> getNotifications(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return VsResponseUtil.success(
                notificationService.getNotifications(principal.getId(), pageable));
    }

    @Tag(name = "notification-controller")
    @Operation(summary = "Get unread notification count")
    @GetMapping(UrlConstant.Notification.UNREAD_COUNT)
    public ResponseEntity<RestData<Long>> getUnreadCount(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(
                notificationService.getUnreadCount(principal.getId()));
    }

    @Tag(name = "notification-controller")
    @Operation(summary = "Mark a notification as read")
    @PatchMapping(UrlConstant.Notification.MARK_READ)
    public ResponseEntity<RestData<CommonResponseDto>> markAsRead(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PathVariable String notificationId) {
        notificationService.markAsRead(principal.getId(), notificationId);
        return VsResponseUtil.success(new CommonResponseDto(true, SuccessMessage.Notification.MARK_READ));
    }

    @Tag(name = "notification-controller")
    @Operation(summary = "Mark all notifications as read")
    @PatchMapping(UrlConstant.Notification.MARK_ALL_READ)
    public ResponseEntity<RestData<CommonResponseDto>> markAllAsRead(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        notificationService.markAllAsRead(principal.getId());
        return VsResponseUtil.success(new CommonResponseDto(true, SuccessMessage.Notification.MARK_ALL_READ));
    }

    @Tag(name = "notification-controller")
    @Operation(summary = "Delete a single notification")
    @DeleteMapping(UrlConstant.Notification.DELETE_ONE)
    public ResponseEntity<RestData<CommonResponseDto>> deleteNotification(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PathVariable String notificationId) {
        notificationService.deleteNotification(principal.getId(), notificationId);
        return VsResponseUtil.success(new CommonResponseDto(true, SuccessMessage.Notification.DELETED));
    }

    @Tag(name = "notification-controller")
    @Operation(summary = "Delete selected notifications")
    @DeleteMapping(UrlConstant.Notification.DELETE_SELECTED)
    public ResponseEntity<RestData<CommonResponseDto>> deleteSelectedNotifications(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @Valid @RequestBody DeleteNotificationsRequestDto dto) {
        notificationService.deleteNotifications(principal.getId(), dto.getNotificationIds());
        return VsResponseUtil.success(new CommonResponseDto(true, SuccessMessage.Notification.DELETED));
    }

    @Tag(name = "notification-controller")
    @Operation(summary = "Delete all notifications")
    @DeleteMapping(UrlConstant.Notification.DELETE_ALL)
    public ResponseEntity<RestData<CommonResponseDto>> deleteAllNotifications(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        notificationService.deleteAllNotifications(principal.getId());
        return VsResponseUtil.success(new CommonResponseDto(true, SuccessMessage.Notification.DELETED_ALL));
    }
}
