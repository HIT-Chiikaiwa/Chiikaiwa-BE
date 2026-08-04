package org.hit.chiikaiwabe.job;

import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.config.properties.BookingProperties;
import org.hit.chiikaiwabe.domain.entity.BookingParticipant;
import org.hit.chiikaiwabe.domain.entity.Notification;
import org.hit.chiikaiwabe.domain.entity.OfflineBooking;
import org.hit.chiikaiwabe.domain.enums.BookingStatus;
import org.hit.chiikaiwabe.domain.enums.NotificationType;
import org.hit.chiikaiwabe.repository.OfflineBookingRepository;
import org.hit.chiikaiwabe.service.NotificationService;
import org.hit.chiikaiwabe.service.PushNotificationService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@Component
@RequiredArgsConstructor
public class BookingReminderJob {
    private final OfflineBookingRepository offlineBookingRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PushNotificationService pushNotificationService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final BookingProperties bookingProperties;
    private final NotificationService notificationService;
    private final MessageSource messageSource;

    @Scheduled(fixedRate = 300000) //5p
    @Transactional
    public void expirePendingBookings(){
        int expireMinutes = bookingProperties.getPendingExpireMinutes() > 0 ?
                bookingProperties.getPendingExpireMinutes() : 30;
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(expireMinutes);
        List<OfflineBooking> pendingBookings = offlineBookingRepository.findExpiredPending(threshold);

        for(OfflineBooking booking : pendingBookings){
            booking.setStatus(BookingStatus.EXPIRED);

            notifyWebSocket(booking.getCreator().getId(), booking);
            if(booking.getParticipants() != null){
                for(BookingParticipant p : booking.getParticipants()){
                    if(p.getUser() != null){
                        notifyWebSocket(p.getUser().getId(), booking);
                    }
                }
            }
        }
        offlineBookingRepository.saveAll(pendingBookings);
    }

    @Scheduled(fixedRate = 900000) //15p
    @Transactional(readOnly = true)
    public void sendBookingReminders(){
        LocalDateTime now = LocalDateTime.now();
        int reminderMinutes = bookingProperties.getDefaultReminderMinutes() > 0 ?
                bookingProperties.getDefaultReminderMinutes() : 60;
        LocalDateTime reminderCutoff = now.plusMinutes(reminderMinutes);
        List<OfflineBooking> upcomingBookings = offlineBookingRepository.findUpcomingForReminder(now, reminderCutoff);
        for(OfflineBooking booking : upcomingBookings){
            String redisKey = "booking:reminded:" + booking.getId();
            Boolean alreadyReminded = redisTemplate.hasKey(redisKey);
            if(Boolean.FALSE.equals(alreadyReminded)){
                String message = "Cuộc hẹn sắp bắt đầu";
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                // Creator
                pushNotificationService.sendPushNotification(booking.getCreator()
                        .getId(), "Nhắc nhở cuộc hẹn", message);
                String partnerName = getPartnerName(booking, booking.getCreator().getId());
                String creatorContent = messageSource.getMessage("notification.booking.reminder",
                        new Object[]{partnerName, booking.getScheduledAt().format(timeFormatter)},
                        LocaleContextHolder.getLocale());
                Notification creatorNotif = notificationService.saveNotification(
                        booking.getCreator().getId(),
                        booking.getParticipants() != null && !booking.getParticipants().isEmpty()
                                ? booking.getParticipants().get(0).getUser() : booking.getCreator(),
                        NotificationType.BOOKING_REMINDER,
                        creatorContent, booking.getId(), "BOOKING");
                notificationService.publishNotification(creatorNotif);

                // Participants
                if(booking.getParticipants() != null){
                    for(BookingParticipant p : booking.getParticipants()){
                        if(p.getUser() != null){
                            pushNotificationService.sendPushNotification(p.getUser()
                                    .getId(), "Nhắc nhở cuộc hẹn", message);
                            String pContent = messageSource.getMessage("notification.booking.reminder",
                                    new Object[]{booking.getCreator().getLastName() + " " + booking.getCreator().getFirstName(),
                                            booking.getScheduledAt().format(timeFormatter)},
                                    LocaleContextHolder.getLocale());
                            Notification pNotif = notificationService.saveNotification(
                                    p.getUser().getId(), booking.getCreator(),
                                    NotificationType.BOOKING_REMINDER,
                                    pContent, booking.getId(), "BOOKING");
                            notificationService.publishNotification(pNotif);
                        }
                    }
                }
                redisTemplate.opsForValue().set(redisKey, "true", 2, TimeUnit.HOURS);
            }
        }
    }

    @Scheduled(fixedRate = 3600000)//1h
    @Transactional
    public void expireConfirmedBookings(){
        int expireHours = bookingProperties.getConfirmedExpireHours() > 0 ?
                bookingProperties.getConfirmedExpireHours() : 24;
        LocalDateTime threshold = LocalDateTime.now().minusHours(expireHours);
        List<OfflineBooking> confirmBookings = offlineBookingRepository.findExpiredConfirmed(threshold);
        for(OfflineBooking b : confirmBookings){
            b.setStatus(BookingStatus.EXPIRED);
        }
        offlineBookingRepository.saveAll(confirmBookings);
    }

    private void notifyWebSocket(String userId, OfflineBooking booking){
        simpMessagingTemplate.convertAndSendToUser(userId, "/queue/booking",
                "Booking " + booking.getId() + " is expired");
    }

    private String getPartnerName(OfflineBooking booking, String userId) {
        if (booking.getCreator().getId().equals(userId)) {
            if (booking.getParticipants() != null && !booking.getParticipants().isEmpty()) {
                BookingParticipant p = booking.getParticipants().get(0);
                return p.getUser().getLastName() + " " + p.getUser().getFirstName();
            }
        } else {
            return booking.getCreator().getLastName() + " " + booking.getCreator().getFirstName();
        }
        return messageSource.getMessage("notification.booking.partner.fallback",
                null, "đối tác", LocaleContextHolder.getLocale());
    }
}
