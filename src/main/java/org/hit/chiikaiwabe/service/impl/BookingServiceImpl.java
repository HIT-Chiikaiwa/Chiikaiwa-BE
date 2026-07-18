package org.hit.chiikaiwabe.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hit.chiikaiwabe.domain.dto.request.CreateBookingRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.BookingResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.MessageResponseDto;
import org.hit.chiikaiwabe.domain.entity.*;
import org.hit.chiikaiwabe.domain.enums.BookingStatus;
import org.hit.chiikaiwabe.domain.enums.MessageType;
import org.hit.chiikaiwabe.domain.enums.ParticipantStatus;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.exception.InternalServerException;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.hit.chiikaiwabe.domain.mapper.MessageMapper;
import org.hit.chiikaiwabe.repository.*;
import org.hit.chiikaiwabe.service.BookingService;
import org.hit.chiikaiwabe.service.ChatNotificationService;
import org.hit.chiikaiwabe.service.PushNotificationService;
import org.hit.chiikaiwabe.config.properties.BookingProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final OfflineBookingRepository bookingRepository;
    private final BookingParticipantRepository participantRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ConversationMemberRepository memberRepository;
    private final ChatNotificationService chatNotificationService;
    private final PushNotificationService pushNotificationService;
    private final ObjectMapper objectMapper;
    private final MessageMapper messageMapper;
    private final MessageSource messageSource;
    private final BookingProperties bookingProperties;

    @Override
    @Transactional
    public BookingResponseDto createBooking(String userId, String conversationId, CreateBookingRequestDto requestDto) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Chat.ERR_CONVERSATION_NOT_FOUND));

        memberRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ForbiddenException(ErrorMessage.Chat.ERR_NOT_MEMBER));

        List<ConversationMember> members = memberRepository.findByConversationId(conversationId);
        if (members.size() != 2) {
            throw new InvalidException(ErrorMessage.Booking.ERR_ONLY_1_ON_1);
        }

        User partner = members.stream()
                .map(ConversationMember::getUser)
                .filter(u -> !u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new InvalidException(ErrorMessage.Booking.ERR_PARTNER_NOT_FOUND));

        if (requestDto.getScheduledAt().isBefore(LocalDateTime.now().plusMinutes(bookingProperties.getMinAdvanceMinutes()))) {
            throw new InvalidException(ErrorMessage.Booking.ERR_SCHEDULED_AT_FUTURE);
        }

        long activeCount = bookingRepository.countByConversationIdAndStatusIn(conversationId,
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));
        if (activeCount >= bookingProperties.getMaxActiveBookings()) {
            throw new InvalidException(ErrorMessage.Booking.ERR_LIMIT_EXCEEDED);
        }

        OfflineBooking booking = OfflineBooking.builder()
                .creator(creator)
                .conversation(conversation)
                .status(BookingStatus.PENDING)
                .subject(requestDto.getSubject())
                .scheduledAt(requestDto.getScheduledAt())
                .durationMinutes(requestDto.getDurationMinutes())
                .locationName(requestDto.getLocationName())
                .locationAddress(requestDto.getLocationAddress())
                .locationDistrict(requestDto.getLocationDistrict())
                .locationCity(requestDto.getLocationCity())
                .note(requestDto.getNote())
                .isRecurring(requestDto.getIsRecurring() != null ? requestDto.getIsRecurring() : false)
                .reminderMinutesBefore(requestDto.getReminderMinutesBefore())
                .build();

        booking = bookingRepository.save(booking);

        BookingParticipant participant = BookingParticipant.builder()
                .booking(booking)
                .user(partner)
                .status(ParticipantStatus.PENDING)
                .reminderMinutesBefore(requestDto.getReminderMinutesBefore())
                .build();

        participant = participantRepository.save(participant);
        List<BookingParticipant> pList = new ArrayList<>();
        pList.add(participant);
        booking.setParticipants(pList);

        Map<String, Object> payload = new HashMap<>();
        payload.put("bookingId", booking.getId());
        payload.put("subject", booking.getSubject());
        payload.put("location", booking.getLocationName());
        payload.put("scheduledAt", booking.getScheduledAt().toString());
        payload.put("duration", booking.getDurationMinutes() != null ? booking.getDurationMinutes() : 0);
        payload.put("note", booking.getNote() != null ? booking.getNote() : "");
        payload.put("status", booking.getStatus().name());
        String content;
        try {
            content = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new InternalServerException(ErrorMessage.Booking.ERR_SERIALIZE_PAYLOAD);
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(creator)
                .content(content)
                .messageType(MessageType.SCHEDULE_INVITE)
                .build();

        message = messageRepository.save(message);

        booking.setMessageId(message.getId());

        conversation.setLastMessage(message);

        MessageResponseDto messageDto = messageMapper.toDto(message);

        String title = messageSource.getMessage(SuccessMessage.Booking.PUSH_NEW_REQUEST_TITLE, null, LocaleContextHolder.getLocale());
        String body = messageSource.getMessage(SuccessMessage.Booking.PUSH_NEW_REQUEST_BODY, new Object[]{creator.getFirstName() + " " + creator.getLastName()}, LocaleContextHolder.getLocale());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                chatNotificationService.broadcastSystemEvent(conversationId, messageDto);
                pushNotificationService.sendPushNotification(partner.getId(), title, body);
            }
        });

        return mapToDto(booking);
    }

    @Override
    @Transactional
    public BookingResponseDto acceptBooking(String userId, String bookingId) {
        OfflineBooking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Booking.ERR_NOT_FOUND));

        BookingParticipant participant = participantRepository.findByBookingIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new ForbiddenException(ErrorMessage.Booking.ERR_NOT_PARTICIPANT));

        if (booking.getStatus() != BookingStatus.PENDING || participant.getStatus() != ParticipantStatus.PENDING) {
            throw new InvalidException(ErrorMessage.Booking.ERR_NOT_PENDING);
        }

        participant.setStatus(ParticipantStatus.ACCEPTED);
        participant.setRespondedAt(LocalDateTime.now());

        booking.setStatus(BookingStatus.CONFIRMED);

        String title = messageSource.getMessage(SuccessMessage.Booking.PUSH_ACCEPTED_TITLE, null, LocaleContextHolder.getLocale());
        String body = messageSource.getMessage(SuccessMessage.Booking.PUSH_ACCEPTED_BODY, new Object[]{participant.getUser().getFirstName() + " " + participant.getUser().getLastName()}, LocaleContextHolder.getLocale());
        if (booking.getMessageId() != null) {
            messageRepository.findById(booking.getMessageId()).ifPresent(msg -> {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> payload = objectMapper.readValue(msg.getContent(), Map.class);
                    payload.put("status", BookingStatus.CONFIRMED.name());
                    msg.setContent(objectMapper.writeValueAsString(payload));

                    MessageResponseDto messageDto = messageMapper.toDto(msg);

                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            chatNotificationService.broadcastSystemEvent(booking.getConversation().getId(), messageDto);
                            pushNotificationService.sendPushNotification(booking.getCreator().getId(), title, body);
                        }
                    });
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new InternalServerException(ErrorMessage.Booking.ERR_SERIALIZE_PAYLOAD);
                }
            });
        } else {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    pushNotificationService.sendPushNotification(booking.getCreator().getId(), title, body);
                }
            });
        }

        return mapToDto(booking);
    }

    @Override
    @Transactional
    public BookingResponseDto rejectBooking(String userId, String bookingId) {
        OfflineBooking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Booking.ERR_NOT_FOUND));

        BookingParticipant participant = participantRepository.findByBookingIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new ForbiddenException(ErrorMessage.Booking.ERR_NOT_PARTICIPANT));

        if (booking.getStatus() != BookingStatus.PENDING || participant.getStatus() != ParticipantStatus.PENDING) {
            throw new InvalidException(ErrorMessage.Booking.ERR_NOT_PENDING);
        }

        participant.setStatus(ParticipantStatus.REJECTED);
        participant.setRespondedAt(LocalDateTime.now());

        booking.setStatus(BookingStatus.REJECTED);

        String title = messageSource.getMessage(SuccessMessage.Booking.PUSH_REJECTED_TITLE, null, LocaleContextHolder.getLocale());
        String body = messageSource.getMessage(SuccessMessage.Booking.PUSH_REJECTED_BODY, new Object[]{participant.getUser().getFirstName() + " " + participant.getUser().getLastName()}, LocaleContextHolder.getLocale());
        if (booking.getMessageId() != null) {
            messageRepository.findById(booking.getMessageId()).ifPresent(msg -> {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> payload = objectMapper.readValue(msg.getContent(), Map.class);
                    payload.put("status", BookingStatus.REJECTED.name());
                    msg.setContent(objectMapper.writeValueAsString(payload));

                    MessageResponseDto messageDto = messageMapper.toDto(msg);

                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            chatNotificationService.broadcastSystemEvent(booking.getConversation().getId(), messageDto);
                            pushNotificationService.sendPushNotification(booking.getCreator().getId(), title, body);
                        }
                    });
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new InternalServerException(ErrorMessage.Booking.ERR_SERIALIZE_PAYLOAD);
                }
            });
        } else {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    pushNotificationService.sendPushNotification(booking.getCreator().getId(), title, body);
                }
            });
        }

        return mapToDto(booking);
    }

    private BookingResponseDto mapToDto(OfflineBooking booking) {
        List<BookingResponseDto.ParticipantDto> participantDtos = new ArrayList<>();
        if (booking.getParticipants() != null) {
            participantDtos = booking.getParticipants().stream().map(p ->
                    BookingResponseDto.ParticipantDto.builder()
                            .id(p.getId())
                            .userId(p.getUser().getId())
                            .status(p.getStatus())
                            .reminderMinutesBefore(p.getReminderMinutesBefore())
                            .respondedAt(p.getRespondedAt())
                            .build()
            ).collect(Collectors.toList());
        }

        return BookingResponseDto.builder()
                .id(booking.getId())
                .creatorId(booking.getCreator().getId())
                .conversationId(booking.getConversation() != null ? booking.getConversation().getId() : null)
                .messageId(booking.getMessageId())
                .status(booking.getStatus().name())
                .subject(booking.getSubject())
                .scheduledAt(booking.getScheduledAt())
                .durationMinutes(booking.getDurationMinutes())
                .locationName(booking.getLocationName())
                .locationAddress(booking.getLocationAddress())
                .locationDistrict(booking.getLocationDistrict())
                .locationCity(booking.getLocationCity())
                .note(booking.getNote())
                .isRecurring(booking.getIsRecurring())
                .cancelledBy(booking.getCancelledBy())
                .cancelReason(booking.getCancelReason())
                .reminderMinutesBefore(booking.getReminderMinutesBefore())
                .createdDate(booking.getCreatedDate())
                .lastModifiedDate(booking.getLastModifiedDate())
                .participants(participantDtos)
                .build();
    }
}
