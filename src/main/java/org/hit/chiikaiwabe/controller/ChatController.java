package org.hit.chiikaiwabe.controller;

import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.domain.dto.request.ScheduleInviteRequestDto;
import org.hit.chiikaiwabe.security.CurrentUser;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.FileService;
import org.hit.chiikaiwabe.service.MessageFeatureService;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@RequiredArgsConstructor
@RestApiV1
public class ChatController {

    private final FileService fileService;
    private final MessageFeatureService messageFeatureService;

    @PostMapping(value = "/chat/conversations/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@PathVariable String id,
                                        @RequestParam("file") MultipartFile file,
                                        @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        messageFeatureService.validateConversationAccess(principal.getId(), id);
        messageFeatureService.validateNotBlockedInDirectConversation(principal.getId(), id);
        String fileUrl = fileService.uploadFile(file);
        return VsResponseUtil.success(fileUrl);
    }

    @DeleteMapping("/chat/messages/{msgId}")
    public ResponseEntity<?> deleteMessageForMe(@PathVariable String msgId,
                            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        messageFeatureService.deleteMessageForMe(principal.getId(), msgId);
        return VsResponseUtil.success(SuccessMessage.Chat.MESSAGE_DELETED_SUCCESS);
    }

    @PutMapping("/chat/messages/{msgId}/recall")
    public ResponseEntity<?> recallMessage(@PathVariable String msgId,
                            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        messageFeatureService.recallMessage(principal.getId(), msgId);
        return VsResponseUtil.success(SuccessMessage.Chat.MESSAGE_RECALLED_SUCCESS);
    }

    @PostMapping("/chat/conversations/{id}/schedule-invite")
    public ResponseEntity<?> createScheduleInvite(@PathVariable String id,
                            @RequestBody @Valid ScheduleInviteRequestDto requestDto,
                                                  @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(messageFeatureService.createScheduleInvite(principal.getId(), id, requestDto));
    }
}
