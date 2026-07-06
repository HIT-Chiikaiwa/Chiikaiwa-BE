package org.hit.chiikaiwabe.service.impl;

import com.google.firebase.messaging.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.domain.dto.request.RegisterDeviceRequestDto;
import org.hit.chiikaiwabe.domain.entity.UserDevice;
import org.hit.chiikaiwabe.repository.UserDeviceRepository;
import org.hit.chiikaiwabe.service.PushNotificationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PushNotificationServiceImpl implements PushNotificationService {
    private final UserDeviceRepository userDeviceRepository;

    @Override
    @Transactional
    public void sendPushNotification(String userID, String title, String body) {
        List<UserDevice> devices = userDeviceRepository.findByUserIdAndIsActiveTrue(userID);
        if(devices.isEmpty()) return;

        List<UserDevice> errorDevices = new ArrayList<>();

        for(UserDevice d : devices){
            try{
                Message message = Message.builder()
                        .setToken(d.getFcmToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .build();
                FirebaseMessaging.getInstance().send(message);
            } catch (FirebaseMessagingException e) {
                String errorCode = e.getMessagingErrorCode().name();
                if(MessagingErrorCode.UNREGISTERED.name().equals(errorCode)
                || MessagingErrorCode.INVALID_ARGUMENT.name().equals(errorCode)){
                    d.setIsActive(false);
                    errorDevices.add(d);
                }
            }
        }
        if(!errorDevices.isEmpty()){
            userDeviceRepository.saveAll(errorDevices);
        }
    }

    @Override
    @Transactional
    public void registerDevice(String userID, RegisterDeviceRequestDto dto) {
        String fcmToken = dto.getFcmToken();

        Optional<UserDevice> existingDevice = userDeviceRepository.findByFcmToken(fcmToken);

        if(existingDevice.isPresent()){
            UserDevice device = existingDevice.get();
            device.setId(userID);
            device.setIsActive(true);
            userDeviceRepository.save(device);
        }
        else{
            UserDevice newDevice = new UserDevice();
            newDevice.setId(userID);
            newDevice.setFcmToken(fcmToken);
            newDevice.setIsActive(true);
            newDevice.setDeviceType(dto.getDeviceType());

            userDeviceRepository.save(newDevice);
        }
    }

    @Override
    @Transactional
    public void unregisterDevice(String userID, String fcmToken) {
        userDeviceRepository.findByUserIdAndFcmToken(userID, fcmToken)
                .ifPresent(device -> device.setIsActive(false));

    }
}
