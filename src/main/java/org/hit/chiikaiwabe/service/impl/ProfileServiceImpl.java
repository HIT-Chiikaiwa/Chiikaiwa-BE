package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.PublicProfileDto;
import org.hit.chiikaiwabe.domain.dto.response.SubjectDto;
import org.hit.chiikaiwabe.domain.dto.response.UserDto;
import org.hit.chiikaiwabe.domain.entity.Subject;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.UserTitle;
import org.hit.chiikaiwabe.domain.mapper.SubjectMapper;
import org.hit.chiikaiwabe.domain.mapper.UserMapper;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.repository.SubjectRepository;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.service.LocationRadarService;
import org.hit.chiikaiwabe.service.ProfileService;
import org.hit.chiikaiwabe.util.UploadFileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final UserMapper userMapper;
    private final SubjectMapper subjectMapper;
    private final UploadFileUtil uploadFileUtil;
    private final PasswordEncoder passwordEncoder;
    private final LocationRadarService locationRadarService;


    private final org.hit.chiikaiwabe.service.LeaderboardService leaderboardService;


    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId}));
    }

    private void checkUserNotDeleted(User user) {
        if (Boolean.TRUE.equals(user.getDeleteFlag())) {
            throw new InvalidException(ErrorMessage.User.ERR_USER_ALREADY_DELETED);
        }
    }


    @Override
    @Cacheable(value = "publicProfile", key = "#userId")
    public PublicProfileDto getPublicProfile(String userId) {
        User user = findUserById(userId);
        checkUserNotDeleted(user);

        List<SubjectDto> subjects = subjectMapper.toSubjectDtos(
                subjectRepository.findAllByUserId(userId));

        PublicProfileDto dto = new PublicProfileDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAvatar(user.getAvatar());
        dto.setUniversity(user.getUniversity());
        dto.setMajorName(user.getMajorName());
        dto.setGender(user.getGender());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setLocation(user.getLocation());
        dto.setTrustScore(user.getTrustScore());
        dto.setBuddyActive(user.getBuddyActive());
        dto.setStatusTag(user.getStatusTag());
        dto.setSubjects(subjects);


        dto.setExpPoints(user.getExpPoints());
        dto.setTitle(user.getTitle());
        UserTitle userTitle = UserTitle.fromExp(user.getExpPoints());
        dto.setTitleIcon(userTitle.getIcon());
        dto.setRank(leaderboardService.getUserRankNumber(userId, user.getExpPoints()));

        return dto;
    }

    @Override
    @CacheEvict(value = {"publicProfile", "radarUserInfo"}, key = "#userId")
    public UserDto updatePersonalInfo(String userId, PersonalInfoUpdateDto dto) {
        User user = findUserById(userId);
        checkUserNotDeleted(user);

        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            userRepository.findByPhoneNumber(dto.getPhone())
                    .filter(existingUser -> !existingUser.getId().equals(userId))
                    .ifPresent(existingUser -> {
                        throw new InvalidException(ErrorMessage.User.ERR_DUPLICATE_PHONE);
                    });
            user.setPhone(dto.getPhone());
        }

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setGender(dto.getGender());
        user.setDateOfBirth(dto.getDateOfBirth());

        return userMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @CacheEvict(value = {"publicProfile", "radarUserInfo"}, key = "#userId")
    public UserDto uploadAvatar(String userId, MultipartFile file) {
        User user = findUserById(userId);
        checkUserNotDeleted(user);

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidException(ErrorMessage.User.ERR_INVALID_AVATAR);
        }

        if (user.getAvatar() != null && !user.getAvatar().isBlank()) {
            uploadFileUtil.destroyFileWithUrl(user.getAvatar());
        }

        String newAvatarUrl = uploadFileUtil.uploadFile(file);
        user.setAvatar(newAvatarUrl);

        return userMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @CacheEvict(value = {"publicProfile", "radarUserInfo"}, key = "#userId")
    public void deleteUser(String userId) {
        User user = findUserById(userId);
        checkUserNotDeleted(user);

        user.setDeleteFlag(Boolean.TRUE);
        userRepository.save(user);
    }

    @Override
    public void updatePassword(String userId, ChangePasswordDto dto) {
        User user = findUserById(userId);
        checkUserNotDeleted(user);

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new InvalidException(ErrorMessage.Auth.ERR_INCORRECT_PASSWORD);
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new InvalidException(ErrorMessage.Auth.ERR_NOT_MATCH_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }


    @Override
    @CacheEvict(value = {"publicProfile", "radarUserInfo"}, key = "#userId")
    public UserDto updateAcademicInfo(String userId, AcademicInfoUpdateDto dto) {
        User user = findUserById(userId);
        checkUserNotDeleted(user);

        user.setUniversity(dto.getUniversity());
        user.setMajorName(dto.getMajorName());

        return userMapper.toUserDto(userRepository.save(user));
    }


    @Override
    @CacheEvict(value = "publicProfile", key = "#userId")
    public SubjectDto addSubject(String userId, SubjectCreateDto dto) {
        User user = findUserById(userId);
        checkUserNotDeleted(user);

        String type = dto.getType().toUpperCase();
        if (!type.equals("STRENGTH") && !type.equals("NEED_REVIEW")) {
            throw new InvalidException(ErrorMessage.Subject.ERR_INVALID_TYPE);
        }

        List<Subject> existing = subjectRepository.findByUserIdAndName(userId, dto.getName());
        if (!existing.isEmpty()) {
            throw new InvalidException(ErrorMessage.Subject.ERR_DUPLICATE_NAME);
        }

        Subject subject = subjectMapper.toSubject(dto);
        subject.setType(type);
        subject.setUser(user);

        return subjectMapper.toSubjectDto(subjectRepository.save(subject));
    }

    @Override
    public List<SubjectDto> getSubjects(String userId, String type) {
        findUserById(userId);

        List<Subject> subjects;
        if (type != null && !type.isBlank()) {
            subjects = subjectRepository.findByUserIdAndType(userId, type.toUpperCase());
        } else {
            subjects = subjectRepository.findAllByUserId(userId);
        }

        return subjectMapper.toSubjectDtos(subjects);
    }

    @Override
    @CacheEvict(value = "publicProfile", key = "#userId")
    public void deleteSubject(String userId, String subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.Subject.ERR_NOT_FOUND_ID, new String[]{subjectId}));

        if (!Objects.equals(subject.getUser().getId(), userId)) {
            throw new ForbiddenException(ErrorMessage.Subject.ERR_NOT_BELONG_TO_USER);
        }

        subjectRepository.delete(subject);
    }


    @Override
    @CacheEvict(value = {"publicProfile", "radarUserInfo"}, key = "#userId")
    public UserDto updateBuddyStatus(String userId, StatusUpdateDto dto) {
        User user = findUserById(userId);
        checkUserNotDeleted(user);

        user.setBuddyActive(dto.getBuddyActive());
        if(Boolean.FALSE.equals(dto.getBuddyActive())){
            locationRadarService.removeLocation(userId);
        }

        return userMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @CacheEvict(value = {"publicProfile", "radarUserInfo"}, key = "#userId")
    public UserDto updateStatusTag(String userId, StatusTagUpdateDto dto) {
        User user = findUserById(userId);
        checkUserNotDeleted(user);

        user.setStatusTag(dto.getStatusTag());

        return userMapper.toUserDto(userRepository.save(user));
    }

    @Override
    @CacheEvict(value = {"publicProfile", "radarUserInfo"}, key = "#userId")
    public UserDto updateLocation(String userId, LocationUpdateDto dto) {
        User user = findUserById(userId);
        checkUserNotDeleted(user);

        user.setLocation(dto.getLocation());

        return userMapper.toUserDto(userRepository.save(user));
    }

}
