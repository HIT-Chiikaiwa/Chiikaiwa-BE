package org.hit.chiikaiwabe.controller;

import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.base.RestData;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.constant.UrlConstant;
import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.*;
import org.hit.chiikaiwabe.service.ChatService;
import org.hit.chiikaiwabe.service.MessageReactionService;

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
    private final MessageReactionService messageReactionService;


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

    // ========================= NEW FEATURES =========================

    @Tag(name = "chat-controller")
    @Operation(summary = "Search conversations by keyword (group name or user name)")
    @GetMapping(UrlConstant.Chat.SEARCH_CONVERSATIONS)
    public ResponseEntity<RestData<Page<ConversationResponseDto>>> searchConversations(
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return VsResponseUtil.success(
                chatService.searchConversations(principal.getId(), keyword, pageable));
    }

    @Tag(name = "chat-controller")
    @Operation(summary = "Search messages in a conversation by keyword")
    @GetMapping(UrlConstant.Chat.SEARCH_MESSAGES)
    public ResponseEntity<RestData<Page<MessageResponseDto>>> searchMessages(
            @PathVariable String id,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal,
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return VsResponseUtil.success(
                chatService.searchMessages(id, principal.getId(), keyword, pageable));
    }

    @Tag(name = "chat-controller")
    @Operation(summary = "Pin a message")
    @PutMapping(UrlConstant.Chat.PIN_MESSAGE)
    public ResponseEntity<RestData<CommonResponseDto>> pinMessage(
            @PathVariable String msgId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(chatService.pinMessage(principal.getId(), msgId));
    }

    @Tag(name = "chat-controller")
    @Operation(summary = "Unpin a message")
    @PutMapping(UrlConstant.Chat.UNPIN_MESSAGE)
    public ResponseEntity<RestData<CommonResponseDto>> unpinMessage(
            @PathVariable String msgId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(chatService.unpinMessage(principal.getId(), msgId));
    }

    @Tag(name = "chat-controller")
    @Operation(summary = "Get pinned messages of a conversation")
    @GetMapping(UrlConstant.Chat.PINNED_MESSAGES)
    public ResponseEntity<RestData<java.util.List<MessageResponseDto>>> getPinnedMessages(
            @PathVariable String id,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(chatService.getPinnedMessages(id, principal.getId()));
    }

    @Tag(name = "chat-controller")
    @Operation(summary = "Reply to a message")
    @PostMapping(UrlConstant.Chat.REPLY_MESSAGE)
    public ResponseEntity<RestData<MessageResponseDto>> replyToMessage(
            @PathVariable String msgId,
            @RequestParam String content,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(HttpStatus.CREATED,
                chatService.replyToMessage(principal.getId(), msgId, content));
    }

    @Tag(name = "chat-controller")
    @Operation(summary = "Forward a message to another conversation")
    @PostMapping(UrlConstant.Chat.FORWARD_MESSAGE)
    public ResponseEntity<RestData<MessageResponseDto>> forwardMessage(
            @PathVariable String msgId,
            @RequestParam String targetConversationId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(HttpStatus.CREATED,
                chatService.forwardMessage(principal.getId(), msgId, targetConversationId));
    }

    @Tag(name = "chat-controller")
    @Operation(summary = "Add emoji reaction to a message")
    @PostMapping(UrlConstant.Chat.MESSAGE_REACTIONS)
    public ResponseEntity<RestData<CommonResponseDto>> addReaction(
            @PathVariable String msgId,
            @RequestParam String emoji,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(
                messageReactionService.addReaction(principal.getId(), msgId, emoji));
    }

    @Tag(name = "chat-controller")
    @Operation(summary = "Remove emoji reaction from a message")
    @DeleteMapping(UrlConstant.Chat.MESSAGE_REACTIONS)
    public ResponseEntity<RestData<CommonResponseDto>> removeReaction(
            @PathVariable String msgId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(
                messageReactionService.removeReaction(principal.getId(), msgId));
    }

    @Tag(name = "chat-controller")
    @Operation(summary = "Dissolve group (owner only)")
    @DeleteMapping(UrlConstant.Chat.DISSOLVE_GROUP)
    public ResponseEntity<RestData<CommonResponseDto>> dissolveGroup(
            @PathVariable String id,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(chatService.dissolveGroup(principal.getId(), id));
    }

    @Tag(name = "chat-controller")
    @Operation(summary = "Transfer group ownership")
    @PutMapping(UrlConstant.Chat.TRANSFER_OWNERSHIP)
    public ResponseEntity<RestData<CommonResponseDto>> transferOwnership(
            @PathVariable String id,
            @RequestParam String newOwnerId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(
                chatService.transferOwnership(principal.getId(), id, newOwnerId));
    }
}
