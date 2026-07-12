package org.hit.chiikaiwabe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.RestData;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.constant.UrlConstant;
import org.hit.chiikaiwabe.domain.dto.request.RegisterDeviceRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.OnlineStatusResponseDto;
import org.hit.chiikaiwabe.security.CurrentUser;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.OnlineStatusService;
import org.hit.chiikaiwabe.service.PushNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RestApiV1
@RequiredArgsConstructor
public class DeviceController {
    private final PushNotificationService pushNotificationService;
    private final OnlineStatusService onlineStatusService;

    @Tag(name = "device-controller")
    @Operation(summary = "Register device for push notifications")
    @PostMapping(UrlConstant.Device.UPDATE_DEVICE)
    public ResponseEntity<RestData<CommonResponseDto>> registerDevice(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody RegisterDeviceRequestDto dto){

        pushNotificationService.registerDevice(currentUser.getId(), dto);
        return VsResponseUtil.success(new CommonResponseDto(true, SuccessMessage.Device.REGISTER));
    }

    @Tag(name = "device-controller")
    @Operation(summary = "Unregister device from push notifications")
    @DeleteMapping(UrlConstant.Device.UPDATE_DEVICE)
    public ResponseEntity<RestData<CommonResponseDto>> unregisterDevice(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("fcmToken") String fcmToken){

        pushNotificationService.unregisterDevice(currentUser.getId(), fcmToken);
        return VsResponseUtil.success(new CommonResponseDto(true, SuccessMessage.Device.UNREGISTER));
    }

    @Tag(name = "device-controller")
    @Operation(summary = "Get user online status")
    @GetMapping(UrlConstant.User.GET_USER_ONLINE)
    public ResponseEntity<RestData<OnlineStatusResponseDto>> getOnlineStatus(@PathVariable String userId){
        boolean isOnline = onlineStatusService.isOnline(userId);
        OnlineStatusResponseDto response = new OnlineStatusResponseDto();
        response.setUserId(userId);
        response.setIsOnline(isOnline);
        return VsResponseUtil.success(response);
    }
}
