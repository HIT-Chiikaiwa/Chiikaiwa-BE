package org.hit.chiikaiwabe.service.impl;

import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.constant.*;
import org.hit.chiikaiwabe.domain.dto.pagination.*;
import org.hit.chiikaiwabe.domain.dto.request.AdminAccountActionRequestDto;
import org.hit.chiikaiwabe.domain.dto.request.AdminUserSearchRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.AdminActionLogDto;
import org.hit.chiikaiwabe.domain.dto.response.UserDto;
import org.hit.chiikaiwabe.domain.entity.*;
import org.hit.chiikaiwabe.domain.enums.*;
import org.hit.chiikaiwabe.domain.mapper.UserMapper;
import org.hit.chiikaiwabe.exception.*;
import org.hit.chiikaiwabe.repository.*;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.UserService;
import org.hit.chiikaiwabe.util.PaginationUtil;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AdminActionLogRepository actionLogRepository;
    private final UserReportRepository reportRepository;

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional(readOnly = true)
    public UserDto getUserById(String userId) {
        return userMapper.toUserDto(getExistingUser(userId));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional(readOnly = true)
    public PaginationResponseDto<UserDto> getCustomers(PaginationFullRequestDto request) {
        var users = userRepository.searchUsers(request.getKeyword(), PaginationUtil.buildPageable(request, SortByDataConstant.USER));
        return new PaginationResponseDto<>(PaginationUtil.buildPagingMeta(request, SortByDataConstant.USER, users),
                userMapper.toUserDtos(users.getContent()));
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public void lockUser(String userId) {
        changeStatus(userId, new AdminAccountActionRequestDto("No reason supplied through legacy API", null), UserStatus.LOCKED);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public void unlockUser(String userId) {
        changeStatus(userId, new AdminAccountActionRequestDto("No reason supplied through legacy API", null), UserStatus.ACTIVE);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public void lockUser(String userId, AdminAccountActionRequestDto request) {
        changeStatus(userId, request, UserStatus.LOCKED);
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public void unlockUser(String userId, AdminAccountActionRequestDto request) {
        changeStatus(userId, request, UserStatus.ACTIVE);
    }

    private void changeStatus(String userId, AdminAccountActionRequestDto request, UserStatus desiredStatus) {
        if (request == null || request.getReason() == null || request.getReason().isBlank()
                || request.getReason().length() > 1000) {
            throw new InvalidException(ErrorMessage.Admin.INVALID_REASON);
        }
        User user = userRepository.findForAdminUpdate(userId)
                .filter(value -> !Boolean.TRUE.equals(value.getDeleteFlag()))
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId}));
        if (user.getRole() == Role.ADMIN) throw new ForbiddenException(ErrorMessage.User.ERR_CANNOT_LOCK_ADMIN);
        UserStatus previous = user.getUserstatus();
        if (previous != UserStatus.ACTIVE && previous != UserStatus.LOCKED) {
            throw new InvalidException(ErrorMessage.User.ERR_INVALID_LOCK_STATUS);
        }
        UserReport report = null;
        if (request.getReportId() != null && !request.getReportId().isBlank()) {
            report = reportRepository.findById(request.getReportId())
                    .orElseThrow(() -> new NotFoundException(ErrorMessage.Admin.ERR_REPORT_NOT_FOUND));
            if (!userId.equals(report.getReported().getId())) throw new InvalidException(ErrorMessage.Admin.ERR_REPORT_TARGET_MISMATCH);
        }
        if (previous == desiredStatus) return;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal actor) || actor.getId() == null) {
            throw new ForbiddenException(ErrorMessage.FORBIDDEN);
        }
        user.setUserstatus(desiredStatus);
        userRepository.save(user);
        actionLogRepository.save(AdminActionLog.builder()
                .actor(userRepository.getReferenceById(actor.getId())).target(user)
                .action(desiredStatus == UserStatus.LOCKED ? AdminAction.LOCK : AdminAction.UNLOCK)
                .previousStatus(previous).newStatus(desiredStatus).reason(request.getReason().trim()).report(report).build());
    }

    @Override
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional(readOnly = true)
    public PaginationResponseDto<AdminActionLogDto> getAdminActions(String userId, AdminUserSearchRequestDto request) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId});
        }
        var logs = actionLogRepository.findByTarget_Id(userId,
                PageRequest.of(request.getPage(), request.getSize(), Sort.by(Sort.Direction.DESC, "createdDate", "id")));
        var meta = new PagingMeta(logs.getTotalElements(), logs.getTotalPages(), request.getPage(), request.getSize(), "createdDate", "DESC");
        return new PaginationResponseDto<>(meta, logs.getContent().stream().map(AdminActionLogDto::from).toList());
    }

    private User getExistingUser(String userId) {
        return userRepository.findById(userId).filter(user -> !Boolean.TRUE.equals(user.getDeleteFlag()))
                .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId}));
    }

    @Override
    public UserDto getCurrentUser(UserPrincipal principal) {
        return userMapper.toUserDto(userRepository.getUser(principal));
    }
}
