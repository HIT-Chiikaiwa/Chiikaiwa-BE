package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.PublicProfileDto;
import org.hit.chiikaiwabe.domain.dto.response.SubjectDto;
import org.hit.chiikaiwabe.domain.dto.response.UserDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProfileService {

    PublicProfileDto getPublicProfile(String userId);

    UserDto updatePersonalInfo(String currentUserId, String userId, PersonalInfoUpdateDto dto);

    void updatePassword(String currentUserId, String userId, ChangePasswordDto dto);

    UserDto uploadAvatar(String currentUserId, String userId, MultipartFile file);

    void deleteUser(String currentUserId, String userId);

    UserDto updateAcademicInfo(String currentUserId, String userId, AcademicInfoUpdateDto dto);

    SubjectDto addSubject(String currentUserId, String userId, SubjectCreateDto dto);

    List<SubjectDto> getSubjects(String userId, String type);

    void deleteSubject(String currentUserId, String userId, String subjectId);

    UserDto updateBuddyStatus(String currentUserId, String userId, StatusUpdateDto dto);

    UserDto updateStatusTag(String currentUserId, String userId, StatusTagUpdateDto dto);

    UserDto updateLocation(String currentUserId, String userId, LocationUpdateDto dto);

}
