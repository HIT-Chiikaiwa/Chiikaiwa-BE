package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.domain.dto.request.ReportRequestDto;
import org.hit.chiikaiwabe.domain.entity.Conversation;
import org.hit.chiikaiwabe.domain.entity.Message;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.entity.UserReport;
import org.hit.chiikaiwabe.domain.enums.ReportStatus;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.repository.ConversationRepository;
import org.hit.chiikaiwabe.repository.MessageRepository;
import org.hit.chiikaiwabe.repository.UserReportRepository;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {

    private final UserReportRepository userReportRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ReportServiceImpl(UserReportRepository userReportRepository,
                             UserRepository userRepository,
                             ConversationRepository conversationRepository,
                             MessageRepository messageRepository) {
        this.userReportRepository = userReportRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    public void createReport(String reporterId, ReportRequestDto requestDto) {
        // BUG-05 FIX: Ki\u1ec3m tra self-report TR\u01af\u1edaC khi query DB.
        // Tr\u01b0\u1edbc \u0111\u00e2y: query 2 l\u1ea7n DB r\u1ed3i m\u1edbi ki\u1ec3m tra \u2192 l\u00e3ng ph\u00ed n\u1ebfu user t\u1ef1 t\u1ed1 m\u00ecnh.
        if (reporterId.equals(requestDto.getReportedId())) {
            throw new org.hit.chiikaiwabe.exception.InvalidException(ErrorMessage.Chat.ERR_CANNOT_REPORT_YOURSELF);
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));
        User reported = userRepository.findById(requestDto.getReportedId())
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID));

        Conversation conversation = null;
        if (requestDto.getConversationId() != null) {
            conversation = conversationRepository.findById(requestDto.getConversationId())
                    .orElse(null);
        }

        Message message = null;
        if (requestDto.getMessageId() != null) {
            message = messageRepository.findById(requestDto.getMessageId())
                    .orElse(null);
        }

        UserReport report = UserReport.builder()
                .reporter(reporter)
                .reported(reported)
                .conversation(conversation)
                .message(message)
                .reason(requestDto.getReason())
                .description(requestDto.getDescription())
                .status(ReportStatus.PENDING)
                .build();

        userReportRepository.save(report);
    }
}
