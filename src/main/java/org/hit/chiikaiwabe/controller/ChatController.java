package org.hit.chiikaiwabe.controller;

import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.domain.dto.request.ScheduleInviteRequestDto;
import org.hit.chiikaiwabe.security.CurrentUser;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.FileService;
import org.hit.chiikaiwabe.service.MessageFeatureService;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.constant.UrlConstant;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import org.hit.chiikaiwabe.annotation.RateLimit;

@RequiredArgsConstructor
@RestApiV1
@RateLimit(capacity = 30, durationInSeconds = 60)
public class ChatController {

    private final FileService fileService;
    private final MessageFeatureService messageFeatureService;

    @RateLimit(capacity = 5, durationInSeconds = 60)
    @PostMapping(value = UrlConstant.Chat.CONVERSATIONS + UrlConstant.Chat.UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@PathVariable String id,
                                        @RequestParam("file") MultipartFile file,
                                        @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        messageFeatureService.validateConversationAccess(principal.getId(), id);
        messageFeatureService.validateNotBlockedInDirectConversation(principal.getId(), id);
        String fileUrl = fileService.uploadFile(file);
        return VsResponseUtil.success(
                messageFeatureService.createFileMessage(principal.getId(), id, fileUrl, file.getOriginalFilename()));
    }

    @DeleteMapping(UrlConstant.Chat.DELETE_MESSAGE)
    public ResponseEntity<?> deleteMessageForMe(@PathVariable String msgId,
                                                @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        messageFeatureService.deleteMessageForMe(principal.getId(), msgId);
        return VsResponseUtil.success(SuccessMessage.Chat.MESSAGE_DELETED_SUCCESS);
    }

    @PutMapping(UrlConstant.Chat.RECALL_MESSAGE)
    public ResponseEntity<?> recallMessage(@PathVariable String msgId,
                                           @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        messageFeatureService.recallMessage(principal.getId(), msgId);
        return VsResponseUtil.success(SuccessMessage.Chat.MESSAGE_RECALLED_SUCCESS);
    }

    @PostMapping(UrlConstant.Chat.CONVERSATIONS + UrlConstant.Chat.SCHEDULE_INVITE)
    public ResponseEntity<?> createScheduleInvite(@PathVariable String id,
                                                  @RequestBody @Valid ScheduleInviteRequestDto requestDto,
                                                  @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(messageFeatureService.createScheduleInvite(principal.getId(), id, requestDto));
    }
}
