package org.hit.chiikaiwabe.controller;

import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.base.RestData;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.constant.UrlConstant;
import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.*;
import org.hit.chiikaiwabe.service.ChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.hit.chiikaiwabe.security.CurrentUser;
import org.hit.chiikaiwabe.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Parameter;

@RequiredArgsConstructor
@RestApiV1
public class ChatRestController {

    private final ChatService chatService;


    @Tag(name = "chat-controller")
    @Operation(summary = "Get user's conversations (paginated)")
    @GetMapping(UrlConstant.Chat.CONVERSATIONS)
    public ResponseEntity<RestData<Page<ConversationResponseDto>>> getConversations(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return VsResponseUtil.success(chatService.getConversations(principal.getId(), pageable));
    }

    @Tag(name = "chat-controller")
    @Operation(summary = "Create or get direct conversation (1:1)")
    @PostMapping(UrlConstant.Chat.DIRECT)
    public ResponseEntity<RestData<ConversationResponseDto>> createDirect(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @Valid @RequestBody CreateDirectConversationRequestDto dto) {
        return VsResponseUtil.success(HttpStatus.CREATED,
                chatService.getOrCreateDirectConversation(principal.getId(), dto));
    }

    @Tag(name = "chat-controller")
    @Operation(summary = "Create group conversation")
    @PostMapping(UrlConstant.Chat.GROUP)
    public ResponseEntity<RestData<ConversationResponseDto>> createGroup(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @Valid @RequestBody CreateGroupRequestDto dto) {
        return VsResponseUtil.success(HttpStatus.CREATED,
                chatService.createGroup(principal.getId(), dto));
    }


    @Tag(name = "chat-controller")
    @Operation(summary = "Get messages of a conversation (paginated - lazy loading)")
    @GetMapping(UrlConstant.Chat.CONVERSATIONS + UrlConstant.Chat.MESSAGES)
    public ResponseEntity<RestData<Page<MessageResponseDto>>> getMessages(
            @PathVariable String id,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return VsResponseUtil.success(chatService.getMessages(id, principal.getId(), pageable));
    }


    @Tag(name = "chat-controller")
    @Operation(summary = "Update group info (name, avatar)")
    @PutMapping(UrlConstant.Chat.CONVERSATIONS + "/{id}")
    public ResponseEntity<RestData<ConversationResponseDto>> updateGroup(
            @PathVariable String id,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @Valid @RequestBody UpdateGroupRequestDto dto) {
        return VsResponseUtil.success(chatService.updateGroup(principal.getId(), id, dto));
    }

    @Tag(name = "chat-controller")
    @Operation(summary = "Add members to group")
    @PostMapping(UrlConstant.Chat.CONVERSATIONS + UrlConstant.Chat.MEMBERS)
    public ResponseEntity<RestData<CommonResponseDto>> addMembers(
            @PathVariable String id,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @Valid @RequestBody AddMembersRequestDto dto) {
        chatService.addMembers(principal.getId(), id, dto);
        return VsResponseUtil.success(HttpStatus.CREATED,
                new CommonResponseDto(true, SuccessMessage.Chat.MEMBER_ADDED));
    }

    @Tag(name = "chat-controller")
    @Operation(summary = "Remove member from group (or leave group)")
    @DeleteMapping(UrlConstant.Chat.CONVERSATIONS + UrlConstant.Chat.MEMBER_DETAIL)
    public ResponseEntity<RestData<CommonResponseDto>> removeMember(
            @PathVariable String id,
            @PathVariable String userId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        chatService.removeMember(principal.getId(), id, userId);
        return VsResponseUtil.success(
                new CommonResponseDto(true, SuccessMessage.Chat.MEMBER_REMOVED));
    }
}
