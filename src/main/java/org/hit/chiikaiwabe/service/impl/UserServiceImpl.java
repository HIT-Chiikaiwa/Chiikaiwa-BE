package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.constant.SortByDataConstant;
import org.hit.chiikaiwabe.domain.dto.pagination.PaginationFullRequestDto;
import org.hit.chiikaiwabe.domain.dto.pagination.PaginationResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.UserDto;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.mapper.UserMapper;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.UserService;
import org.hit.chiikaiwabe.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.hit.chiikaiwabe.domain.enums.Role;
import org.hit.chiikaiwabe.domain.enums.UserStatus;
import org.hit.chiikaiwabe.exception.ForbiddenException;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  private final UserMapper userMapper;

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
    Pageable pageable = PaginationUtil.buildPageable(request, SortByDataConstant.USER);
    Page<User> users = userRepository.searchUsers(request.getKeyword(), pageable);
    return new PaginationResponseDto<>(
        PaginationUtil.buildPagingMeta(request, SortByDataConstant.USER, users),
        userMapper.toUserDtos(users.getContent()));
  }

  @Override
  @PreAuthorize("hasAuthority('ADMIN')")
  @Transactional
  public void lockUser(String userId) {
    User user = getManageableUser(userId);
    if (user.getUserstatus() == UserStatus.LOCKED) {
      return;
    }
    if (user.getUserstatus() != UserStatus.ACTIVE) {
      throw new InvalidException(ErrorMessage.User.ERR_INVALID_LOCK_STATUS);
    }
    user.setUserstatus(UserStatus.LOCKED);
    userRepository.save(user);
  }

  @Override
  @PreAuthorize("hasAuthority('ADMIN')")
  @Transactional
  public void unlockUser(String userId) {
    User user = getManageableUser(userId);
    if (user.getUserstatus() == UserStatus.ACTIVE) {
      return;
    }
    if (user.getUserstatus() != UserStatus.LOCKED) {
      throw new InvalidException(ErrorMessage.User.ERR_INVALID_LOCK_STATUS);
    }
    user.setUserstatus(UserStatus.ACTIVE);
    userRepository.save(user);
  }

  private User getExistingUser(String userId) {
    return userRepository.findById(userId)
        .filter(user -> !Boolean.TRUE.equals(user.getDeleteFlag()))
        .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{userId}));
  }

  private User getManageableUser(String userId) {
    User user = getExistingUser(userId);
    if (user.getRole() == Role.ADMIN) {
      throw new ForbiddenException(ErrorMessage.User.ERR_CANNOT_LOCK_ADMIN);
    }
    return user;
  }

  @Override
  public UserDto getCurrentUser(UserPrincipal principal) {
    User user = userRepository.getUser(principal);
    return userMapper.toUserDto(user);
  }

}
