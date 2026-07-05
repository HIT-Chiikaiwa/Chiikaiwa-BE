package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.domain.dto.request.RegisterDeviceRequestDto;
import org.hit.chiikaiwabe.service.PushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PushNotificationServiceImpl implements PushNotificationService {

    @Override
    public void sendPushNotification(String userId, String title, String body) { /* TODO */ }

    @Override
    public void registerDevice(String userId, RegisterDeviceRequestDto dto) { /* TODO */ }

    @Override
    public void removeDevice(String userId, String fcmToken) { /* TODO */ }
}
