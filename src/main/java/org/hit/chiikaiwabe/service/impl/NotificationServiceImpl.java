package org.hit.chiikaiwabe.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.domain.dto.response.NotificationResponseDto;
import org.hit.chiikaiwabe.domain.entity.Notification;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.NotificationType;
import org.hit.chiikaiwabe.domain.mapper.NotificationMapper;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.repository.NotificationRepository;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.service.NotificationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Executor notificationExecutor;

    private static final String NOTIFICATION_CHANNEL = "notification:push";

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            NotificationMapper notificationMapper,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            @Qualifier("notificationExecutor") Executor notificationExecutor) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationMapper = notificationMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.notificationExecutor = notificationExecutor;
    }

    @Override
    public Notification saveNotification(String recipientId, User actor, NotificationType type,
                                         String content, String targetId, String targetType) {
        User recipient = userRepository.getReferenceById(recipientId);
        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .content(content)
                .targetId(targetId)
                .targetType(targetType)
                .isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    public void publishNotification(Notification notification) {
        // Pre-build DTO trong calling thread (còn JPA session) để tránh LazyInitializationException
        NotificationResponseDto dto = notificationMapper.toDto(notification);
        String recipientUserId = notification.getRecipient().getId();

        // Dispatch async qua Executor trực tiếp (tránh Spring AOP self-invocation issue)
        notificationExecutor.execute(() -> {
            try {
                Map<String, Object> event = new HashMap<>();
                event.put("recipientUserId", recipientUserId);
                event.put("payload", dto);
                redisTemplate.convertAndSend(NOTIFICATION_CHANNEL, objectMapper.writeValueAsString(event));
            } catch (Exception e) {
                log.error("Failed to publish notification: {}", e.getMessage(), e);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getNotifications(String userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByRecipientId(userId, pageable);
        return notifications.map(notificationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(String userId, String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Notification.ERR_NOT_FOUND));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new ForbiddenException(ErrorMessage.Notification.ERR_NOT_RECIPIENT);
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(String userId, String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Notification.ERR_NOT_FOUND));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new ForbiddenException(ErrorMessage.Notification.ERR_NOT_RECIPIENT);
        }

        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public void deleteNotifications(String userId, List<String> notificationIds) {
        notificationRepository.deleteByIdInAndRecipientId(notificationIds, userId);
    }

    @Override
    @Transactional
    public void deleteAllNotifications(String userId) {
        notificationRepository.deleteAllByRecipientId(userId);
    }
}

