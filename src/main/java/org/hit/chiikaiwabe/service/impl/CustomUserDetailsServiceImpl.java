package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.UserStatus;
import org.hit.chiikaiwabe.exception.UnauthorizedException;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.service.CustomUserDetailsService;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService, CustomUserDetailsService {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME,
            new String[]{username}));
    return createActivePrincipal(user);
  }

  @Override
  @Transactional
  public UserDetails loadUserById(String id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_ID, new String[]{id}));
    return createActivePrincipal(user);
  }

  private UserPrincipal createActivePrincipal(User user) {
    if (user.getUserstatus() == UserStatus.LOCKED) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_ACCOUNT_LOCKED);
    }
    if (Boolean.TRUE.equals(user.getDeleteFlag())) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_ACCOUNT_NOT_ENABLED);
    }
    if (user.getUserstatus() != UserStatus.ACTIVE) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_ACCOUNT_NOT_VERIFIED);
    }
    return UserPrincipal.create(user);
  }

}
