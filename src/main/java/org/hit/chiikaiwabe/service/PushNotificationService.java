package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.request.RegisterDeviceRequestDto;

public interface PushNotificationService {
    void sendPushNotification(String userID, String title, String body);
    void registerDevice(String userID, RegisterDeviceRequestDto dto);
    void unregisterDevice(String userID, String fcmToken);
}
