package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.PublicProfileDto;
import org.hit.chiikaiwabe.domain.dto.response.SubjectDto;
import org.hit.chiikaiwabe.domain.dto.response.UserDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProfileService {

    PublicProfileDto getPublicProfile(String userId);

    UserDto updatePersonalInfo(String userId, PersonalInfoUpdateDto dto);

    UserDto uploadAvatar(String userId, MultipartFile file);

    void deleteUser(String userId);


    UserDto updateAcademicInfo(String userId, AcademicInfoUpdateDto dto);

    SubjectDto addSubject(String userId, SubjectCreateDto dto);


    List<SubjectDto> getSubjects(String userId, String type);


    void deleteSubject(String userId, String subjectId);


    UserDto updateBuddyStatus(String userId, StatusUpdateDto dto);


    UserDto updateStatusTag(String userId, StatusTagUpdateDto dto);


    UserDto updateLocation(String userId, LocationUpdateDto dto);

}
