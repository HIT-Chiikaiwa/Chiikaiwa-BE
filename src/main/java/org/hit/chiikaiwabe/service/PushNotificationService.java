package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.request.RegisterDeviceRequestDto;

public interface PushNotificationService {
    void sendPushNotification(String userId, String title, String body);
    void sendPushNotification(String userId, String title, String body, java.util.Map<String, String> data);
    void registerDevice(String userId, RegisterDeviceRequestDto dto);
    void unregisterDevice(String userId, String fcmToken);
}
