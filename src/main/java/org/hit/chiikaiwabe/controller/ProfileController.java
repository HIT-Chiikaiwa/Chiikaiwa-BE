package org.hit.chiikaiwabe.controller;

import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.constant.UrlConstant;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.*;
import org.hit.chiikaiwabe.base.RestData;
import org.hit.chiikaiwabe.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.hit.chiikaiwabe.annotation.RateLimit;

import org.hit.chiikaiwabe.security.CurrentUser;
import org.hit.chiikaiwabe.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestApiV1
@RateLimit(capacity = 20, durationInSeconds = 60)
public class ProfileController {

    private final ProfileService profileService;


    @Tag(name = "profile-controller")
    @Operation(summary = "Get public profile")
    @GetMapping(UrlConstant.Profile.GET_PROFILE)
    public ResponseEntity<RestData<PublicProfileDto>> getPublicProfile(@PathVariable String userId) {
        return VsResponseUtil.success(profileService.getPublicProfile(userId));
    }

    @Tag(name = "profile-controller")
    @Operation(summary = "Update personal info")
    @RateLimit(capacity = 1, durationInSeconds = 120)
    @PutMapping(UrlConstant.Profile.UPDATE_PERSONAL_INFO)
    public ResponseEntity<RestData<UserDto>> updatePersonalInfo(
            @PathVariable String userId,
            @Valid @RequestBody PersonalInfoUpdateDto dto,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(profileService.updatePersonalInfo(principal.getId(), userId, dto));
    }

    @Tag(name = "profile-controller")
    @Operation(summary = "Change password")
    @PutMapping(UrlConstant.Profile.CHANGE_PASSWORD)
    public ResponseEntity<RestData<CommonResponseDto>> changePassword(
            @PathVariable String userId,
            @Valid @RequestBody ChangePasswordDto dto,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        profileService.updatePassword(principal.getId(), userId, dto);
        return VsResponseUtil.success(
                new CommonResponseDto(true, SuccessMessage.PASSWORD_UPDATED)
        );
    }

    @Tag(name = "profile-controller")
    @Operation(summary = "Upload avatar")
    @RateLimit(capacity = 1, durationInSeconds = 120)
    @PostMapping(value = UrlConstant.Profile.UPLOAD_AVATAR, consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RestData<UserDto>> uploadAvatar(
            @PathVariable String userId,
            @Parameter(description = "Avatar file to upload")
            @RequestPart("file") MultipartFile file,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(profileService.uploadAvatar(principal.getId(), userId, file));
    }

    @Tag(name = "profile-controller")
    @Operation(summary = "Delete account (soft delete)")
    @DeleteMapping(UrlConstant.Profile.DELETE_USER)
    public ResponseEntity<RestData<CommonResponseDto>> deleteUser(
            @PathVariable String userId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        profileService.deleteUser(principal.getId(), userId);
        return VsResponseUtil.success(
                new CommonResponseDto(true, SuccessMessage.USER_DELETED)
        );
    }


    @Tag(name = "profile-controller")
    @Operation(summary = "Update academic info (university, major)")
    @RateLimit(capacity = 1, durationInSeconds = 120)
    @PutMapping(UrlConstant.Profile.UPDATE_ACADEMIC_INFO)
    public ResponseEntity<RestData<UserDto>> updateAcademicInfo(
            @PathVariable String userId,
            @Valid @RequestBody AcademicInfoUpdateDto dto,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(profileService.updateAcademicInfo(principal.getId(), userId, dto));
    }


    @Tag(name = "profile-controller")
    @Operation(summary = "Add new subject")
    @PostMapping(UrlConstant.Profile.ADD_SUBJECT)
    public ResponseEntity<RestData<SubjectDto>> addSubject(
            @PathVariable String userId,
            @Valid @RequestBody SubjectCreateDto dto,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(HttpStatus.CREATED, profileService.addSubject(principal.getId(), userId, dto));
    }

    @Tag(name = "profile-controller")
    @Operation(summary = "Get user's subject list")
    @GetMapping(UrlConstant.Profile.GET_SUBJECTS)
    public ResponseEntity<RestData<List<SubjectDto>>> getSubjects(
            @PathVariable String userId,
            @RequestParam(required = false) String type) {
        return VsResponseUtil.success(profileService.getSubjects(userId, type));
    }

    @Tag(name = "profile-controller")
    @Operation(summary = "Delete subject")
    @DeleteMapping(UrlConstant.Profile.DELETE_SUBJECT)
    public ResponseEntity<RestData<CommonResponseDto>> deleteSubject(
            @PathVariable String userId,
            @PathVariable String subjectId,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        profileService.deleteSubject(principal.getId(), userId, subjectId);
        return VsResponseUtil.success(
                new CommonResponseDto(true, SuccessMessage.SUBJECT_DELETED)
        );
    }


    @Tag(name = "profile-controller")
    @Operation(summary = "Toggle buddy status")
    @PatchMapping(UrlConstant.Profile.UPDATE_BUDDY_STATUS)
    public ResponseEntity<RestData<UserDto>> updateBuddyStatus(
            @PathVariable String userId,
            @Valid @RequestBody StatusUpdateDto dto,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(profileService.updateBuddyStatus(principal.getId(), userId, dto));
    }

    @Tag(name = "profile-controller")
    @Operation(summary = "Update status tag")
    @RateLimit(capacity = 1, durationInSeconds = 120)
    @PutMapping(UrlConstant.Profile.UPDATE_STATUS_TAG)
    public ResponseEntity<RestData<UserDto>> updateStatusTag(
            @PathVariable String userId,
            @RequestBody StatusTagUpdateDto dto,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(profileService.updateStatusTag(principal.getId(), userId, dto));
    }

    @Tag(name = "profile-controller")
    @Operation(summary = "Update location")
    @RateLimit(capacity = 1, durationInSeconds = 120)
    @PutMapping(UrlConstant.Profile.UPDATE_LOCATION)
    public ResponseEntity<RestData<UserDto>> updateLocation(
            @PathVariable String userId,
            @Valid @RequestBody LocationUpdateDto dto,
            @Parameter(hidden = true) @CurrentUser UserPrincipal principal) {
        return VsResponseUtil.success(profileService.updateLocation(principal.getId(), userId, dto));
    }

}
