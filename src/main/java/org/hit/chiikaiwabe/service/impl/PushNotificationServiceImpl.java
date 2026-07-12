package org.hit.chiikaiwabe.service.impl;

import com.google.firebase.messaging.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.domain.dto.request.RegisterDeviceRequestDto;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.entity.UserDevice;
import org.hit.chiikaiwabe.repository.UserDeviceRepository;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.service.PushNotificationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PushNotificationServiceImpl implements PushNotificationService {
    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;

    @Async("notificationExecutor")
    @Override
    public void sendPushNotification(String userID, String title, String body) {
        List<UserDevice> devices = userDeviceRepository.findByUserIdAndIsActiveTrue(userID);
        if (devices.isEmpty()) return;

        List<String> tokens = devices.stream()
                .map(UserDevice::getFcmToken)
                .toList();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            if (response.getFailureCount() > 0) {
                List<UserDevice> errorDevices = new ArrayList<>();
                List<SendResponse> responses = response.getResponses();

                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        FirebaseMessagingException e = responses.get(i).getException();
                        MessagingErrorCode errorCode = e.getMessagingErrorCode();

                        if (errorCode == MessagingErrorCode.UNREGISTERED
                                || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                            UserDevice d = devices.get(i);
                            d.setIsActive(false);
                            errorDevices.add(d);
                        }
                    }
                }

                if (!errorDevices.isEmpty()) {
                    userDeviceRepository.saveAll(errorDevices);
                }
            }
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void registerDevice(String userID, RegisterDeviceRequestDto dto) {
        String fcmToken = dto.getFcmToken();

        Optional<UserDevice> existingDevice = userDeviceRepository.findByFcmToken(fcmToken);
        User existUser = userRepository.findById(userID)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));

        if(existingDevice.isPresent()){
            UserDevice device = existingDevice.get();
            device.setUser(existUser);
            device.setDeviceName(dto.getDeviceName());
            device.setIsActive(true);
            userDeviceRepository.save(device);
        }
        else{
            UserDevice newDevice = new UserDevice();
            newDevice.setUser(existUser);
            newDevice.setFcmToken(fcmToken);
            newDevice.setIsActive(true);
            newDevice.setDeviceName(dto.getDeviceName());
            newDevice.setDeviceType(dto.getDeviceType());

            userDeviceRepository.save(newDevice);
        }
    }

    @Override
    @Transactional
    public void unregisterDevice(String userID, String fcmToken) {
        userDeviceRepository.findByUserIdAndFcmToken(userID, fcmToken)
                .ifPresent(device -> {
                    device.setIsActive(false);
                    userDeviceRepository.save(device);
                });

    }
}
