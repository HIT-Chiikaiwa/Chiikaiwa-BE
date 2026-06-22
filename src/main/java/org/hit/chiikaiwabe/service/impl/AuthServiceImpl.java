package org.hit.chiikaiwabe.service.impl;

import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.constant.RoleConstant;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.LoginResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.TokenRefreshResponseDto;
import org.hit.chiikaiwabe.domain.entity.Role;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.exception.UnauthorizedException;
import org.hit.chiikaiwabe.repository.RoleRepository;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.security.jwt.JwtTokenProvider;
import org.hit.chiikaiwabe.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.service.CustomUserDetailsService;
import org.hit.chiikaiwabe.service.OtpService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final AuthenticationManager authenticationManager;

  private final JwtTokenProvider jwtTokenProvider;

  private final CustomUserDetailsService customUserDetailsService;

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  private final OtpService otpService;

  private final StringRedisTemplate redisTemplate;

  private final MessageSource messageSource;
  @Override
  public LoginResponseDto login(LoginRequestDto request) {
    try {
      User user = userRepository.findByEmail(request.getEmailOrPhone())
              .orElseThrow(() -> new UnauthorizedException(ErrorMessage.Auth.ERR_INCORRECT_USERNAME));
      if ("UNVERIFIED".equals(user.getStatus())) {
        throw new UnauthorizedException(ErrorMessage.Auth.ERR_ACCOUNT_NOT_VERIFIED);
      }
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getEmailOrPhone(), request.getPassword()));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      String accessToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.FALSE);
      String refreshToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.TRUE);

      long accessTtl = jwtTokenProvider.extractExpirationFromJwt(accessToken).getTime() - System.currentTimeMillis();
      long refreshTtl = jwtTokenProvider.extractExpirationFromJwt(refreshToken).getTime() - System.currentTimeMillis();

      if (accessTtl > 0) {
        redisTemplate.opsForValue().set(accessToken, userPrincipal.getId(), accessTtl, TimeUnit.MILLISECONDS);
      }
      if (refreshTtl > 0) {
        redisTemplate.opsForValue().set(refreshToken, userPrincipal.getId(), refreshTtl, TimeUnit.MILLISECONDS);
      }
      return new LoginResponseDto(accessToken, refreshToken, userPrincipal.getId(), authentication.getAuthorities());
    } catch (InternalAuthenticationServiceException e) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_INCORRECT_USERNAME);
    } catch (BadCredentialsException e) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_INCORRECT_PASSWORD);
    }
  }

  @Override
  public TokenRefreshResponseDto refresh(TokenRefreshRequestDto request) {
    try {
      String oldRefreshToken = request.getRefreshToken();

      if (Boolean.FALSE.equals(redisTemplate.hasKey(oldRefreshToken))) {
        throw new UnauthorizedException(ErrorMessage.Auth.INVALID_REFRESH_TOKEN);
      }
      Authentication authentication = jwtTokenProvider.getAuthenticationByRefreshToken(request.getRefreshToken());
      String identifier = authentication.getName();
      UserPrincipal userPrincipal = (UserPrincipal) customUserDetailsService.loadUserByUsername(identifier);
      String accessToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.FALSE);
      String refreshToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.TRUE);

      redisTemplate.delete(oldRefreshToken);
      long accessTtl = jwtTokenProvider.extractExpirationFromJwt(accessToken).getTime() - System.currentTimeMillis();
      long refreshTtl = jwtTokenProvider.extractExpirationFromJwt(refreshToken).getTime() - System.currentTimeMillis();

      if (accessTtl > 0) {
        redisTemplate.opsForValue().set(accessToken, userPrincipal.getId(), accessTtl, TimeUnit.MILLISECONDS);
      }
      if (refreshTtl > 0) {
        redisTemplate.opsForValue().set(refreshToken, userPrincipal.getId(), refreshTtl, TimeUnit.MILLISECONDS);
      }
      return new TokenRefreshResponseDto(accessToken, refreshToken);

    } catch (Exception e) {
      throw new UnauthorizedException(ErrorMessage.Auth.INVALID_REFRESH_TOKEN);
    }
  }

  @Override
  public CommonResponseDto logout( HttpServletRequest request, String refreshToken) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      String accessToken = bearerToken.substring(7);
      redisTemplate.delete(accessToken);
    }
    if (StringUtils.hasText(refreshToken)) {
      redisTemplate.delete(refreshToken);
    }

    SecurityContextHolder.clearContext();
    return new CommonResponseDto(true, SuccessMessage.LOGOUT_SUCCESS);
  }


  @Override
  public CommonResponseDto register(UserCreateDto request) {
    if (!request.getPassword().equals(request.getConfirmPassword())) {
      throw new InvalidException(ErrorMessage.Auth.ERR_CONFIRM_PASSWORD_NOT_MATCH);
    }
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new InvalidException(ErrorMessage.Auth.ERR_ACCOUNT_ALREADY_EXISTS);
    }

    int currentYear = java.time.Year.now().getValue();
    int age = 0;
    if (request.getDateOfBirth() != null) {
      age = java.time.Period.between(request.getDateOfBirth(), java.time.LocalDate.now()).getYears();
    }

    Role userRole = roleRepository.findByRoleName(RoleConstant.USER);
    if (userRole == null) {
      throw new NotFoundException(ErrorMessage.Role.ERR_ROLE_NOT_FOUND);
    }

    User newUser = User.builder()
            .username(request.getEmail())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .gender(request.getGender())
            .dateOfBirth(request.getDateOfBirth())
            .age(age)
            .location("Chưa cập nhật")
            .status("UNVERIFIED")
            .trustScore(100.0)
            .role(userRole)
            .build();

    userRepository.save(newUser);

    otpService.generateAndSendOtp(request.getEmail());
    return new CommonResponseDto(true, SuccessMessage.REGISTER_SUCCESS_CHECK_EMAIL);
  }

  @Override
  public CommonResponseDto verifyRegisterOtp(VerifyOtpRequestDto request) {
    boolean isValid = otpService.validateOtp(request.getEmail(), request.getOtpCode());
    if (!isValid) {
      throw new InvalidException(ErrorMessage.Auth.ERR_OTP_INCORRECT);
    }
    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME, new String[]{request.getEmail()}));

    if ("ACTIVE".equals(user.getStatus())) {
      throw new InvalidException(ErrorMessage.Auth.ERR_ACCOUNT_ALREADY_VERIFIED);
    }

    user.setStatus("ACTIVE");
    userRepository.save(user);
    return new CommonResponseDto(true, SuccessMessage.VERIFY_REGISTER_SUCCESS);
  }



  @Override
  public CommonResponseDto forgotPasswordSendOtp(SendOtpRequestDto request) {
    String spamKey = "OTP_RATE_LIMIT:" + request.getEmail();
    if (Boolean.TRUE.equals(redisTemplate.hasKey(spamKey))) {
      throw new InvalidException(ErrorMessage.Auth.ERR_OTP_SPAM);
    }
    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME,
                    new String[]{request.getEmail()}));
    if (!"ACTIVE".equals(user.getStatus())) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_FORGOT_PASS_NOT_VERIFIED);
    }

    otpService.generateAndSendOtp(request.getEmail());

    redisTemplate.opsForValue().set(spamKey, "BLOCKED", 60, TimeUnit.SECONDS);

    String successMsg = messageSource.getMessage(SuccessMessage.FORGOT_PASSWORD_SEND_OTP_SUCCESS,
            null, LocaleContextHolder.getLocale());
    return new CommonResponseDto(true, successMsg);
  }
  @Override
  public CommonResponseDto verifyForgotPasswordOtp(VerifyOtpRequestDto request) {
    boolean isValid = otpService.validateOtp(request.getEmail(), request.getOtpCode());
    if (!isValid) {
      throw new InvalidException(ErrorMessage.Auth.ERR_OTP_INCORRECT);
    }
    String resetTicketKey = "RESET_TICKET:" + request.getEmail();
    redisTemplate.opsForValue().set(resetTicketKey, "VALID", 5, TimeUnit.MINUTES);

    return new CommonResponseDto(true, SuccessMessage.VERIFY_OTP_SUCCESS);
  }

  @Override
  public CommonResponseDto resetPassword(ResetPasswordRequestDto request) {
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new InvalidException(ErrorMessage.Auth.ERR_CONFIRM_PASSWORD_NOT_MATCH);
    }
    String resetTicketKey = "RESET_TICKET:" + request.getEmail();
    if (Boolean.FALSE.equals(redisTemplate.hasKey(resetTicketKey))) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_RESET_TICKET_EXPIRED);
    }
    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME, new String[]{request.getEmail()}));

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
    redisTemplate.delete(resetTicketKey);
    return new CommonResponseDto(true, SuccessMessage.RESET_PASSWORD_SUCCESS);
  }

}
