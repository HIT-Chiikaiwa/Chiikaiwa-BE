package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.response.NotificationResponseDto;
import org.hit.chiikaiwabe.domain.entity.Notification;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    Notification saveNotification(String recipientId, User actor, NotificationType type,
                                  String content, String targetId, String targetType);

    void publishNotification(Notification notification);

    Page<NotificationResponseDto> getNotifications(String userId, Pageable pageable);

    long getUnreadCount(String userId);

    void markAsRead(String userId, String notificationId);

    void markAllAsRead(String userId);

    void deleteNotification(String userId, String notificationId);

    void deleteNotifications(String userId, List<String> notificationIds);

    void deleteAllNotifications(String userId);
}
