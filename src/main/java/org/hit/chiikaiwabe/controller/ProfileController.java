package org.hit.chiikaiwabe.controller;

import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.constant.UrlConstant;
import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@RequiredArgsConstructor
@RestApiV1
public class ProfileController {

    private final ProfileService profileService;


    @Tag(name = "profile-controller")
    @Operation(summary = "Get public profile")
    @GetMapping(UrlConstant.Profile.GET_PROFILE)
    public ResponseEntity<?> getPublicProfile(@PathVariable String userId) {
        return VsResponseUtil.success(profileService.getPublicProfile(userId));
    }

    @Tag(name = "profile-controller")
    @Operation(summary = "Update personal info")
    @PutMapping(UrlConstant.Profile.UPDATE_PERSONAL_INFO)
    public ResponseEntity<?> updatePersonalInfo(
            @PathVariable String userId,
            @Valid @RequestBody PersonalInfoUpdateDto dto) {
        return VsResponseUtil.success(profileService.updatePersonalInfo(userId, dto));
    }

    @Tag(name = "profile-controller")
    @Operation(summary = "Change password")
    @PutMapping("/api/v1/profile/{userId}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable String userId,
            @Valid @RequestBody ChangePasswordDto dto) {
        profileService.updatePassword(userId, dto);
        return VsResponseUtil.success("Password updated successfully");
    }

    @Tag(name = "profile-controller")
    @Operation(summary = "Upload avatar")
    @PostMapping(UrlConstant.Profile.UPLOAD_AVATAR)
    public ResponseEntity<?> uploadAvatar(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file) {
        return VsResponseUtil.success(profileService.uploadAvatar(userId, file));
    }

    @Tag(name = "profile-controller")
    @Operation(summary = "Delete account (soft delete)")
    @DeleteMapping(UrlConstant.Profile.DELETE_USER)
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        profileService.deleteUser(userId);
        return VsResponseUtil.success("User deleted successfully");
    }


    @Tag(name = "profile-controller")
    @Operation(summary = "Update academic info (university, major)")
    @PutMapping(UrlConstant.Profile.UPDATE_ACADEMIC_INFO)
    public ResponseEntity<?> updateAcademicInfo(
            @PathVariable String userId,
            @Valid @RequestBody AcademicInfoUpdateDto dto) {
        return VsResponseUtil.success(profileService.updateAcademicInfo(userId, dto));
    }


    @Tag(name = "profile-controller")
    @Operation(summary = "Add new subject")
    @PostMapping(UrlConstant.Profile.ADD_SUBJECT)
    public ResponseEntity<?> addSubject(
            @PathVariable String userId,
            @Valid @RequestBody SubjectCreateDto dto) {
        return VsResponseUtil.success(HttpStatus.CREATED, profileService.addSubject(userId, dto));
    }

    @Tag(name = "profile-controller")
    @Operation(summary = "Get user's subject list")
    @GetMapping(UrlConstant.Profile.GET_SUBJECTS)
    public ResponseEntity<?> getSubjects(
            @PathVariable String userId,
            @RequestParam(required = false) String type) {
        return VsResponseUtil.success(profileService.getSubjects(userId, type));
    }

    @Tag(name = "profile-controller")
    @Operation(summary = "Delete subject")
    @DeleteMapping(UrlConstant.Profile.DELETE_SUBJECT)
    public ResponseEntity<?> deleteSubject(
            @PathVariable String userId,
            @PathVariable String subjectId) {
        profileService.deleteSubject(userId, subjectId);
        return VsResponseUtil.success("Subject deleted successfully");
    }


    @Tag(name = "profile-controller")
    @Operation(summary = "Toggle buddy status")
    @PatchMapping(UrlConstant.Profile.UPDATE_BUDDY_STATUS)
    public ResponseEntity<?> updateBuddyStatus(
            @PathVariable String userId,
            @Valid @RequestBody StatusUpdateDto dto) {
        return VsResponseUtil.success(profileService.updateBuddyStatus(userId, dto));
    }

    @Tag(name = "profile-controller")
    @Operation(summary = "Update status tag")
    @PutMapping(UrlConstant.Profile.UPDATE_STATUS_TAG)
    public ResponseEntity<?> updateStatusTag(
            @PathVariable String userId,
            @RequestBody StatusTagUpdateDto dto) {
        return VsResponseUtil.success(profileService.updateStatusTag(userId, dto));
    }

    @Tag(name = "profile-controller")
    @Operation(summary = "Update location")
    @PutMapping(UrlConstant.Profile.UPDATE_LOCATION)
    public ResponseEntity<?> updateLocation(
            @PathVariable String userId,
            @Valid @RequestBody LocationUpdateDto dto) {
        return VsResponseUtil.success(profileService.updateLocation(userId, dto));
    }

}
