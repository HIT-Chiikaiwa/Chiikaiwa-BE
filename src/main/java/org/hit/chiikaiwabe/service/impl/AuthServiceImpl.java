package org.hit.chiikaiwabe.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.domain.enums.Role;
import org.hit.chiikaiwabe.domain.enums.UserStatus;
import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.LoginResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.TokenRefreshResponseDto;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.exception.InvalidException;
import org.hit.chiikaiwabe.exception.NotFoundException;
import org.hit.chiikaiwabe.exception.UnauthorizedException;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.security.jwt.JwtTokenProvider;
import org.hit.chiikaiwabe.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.service.CustomUserDetailsService;
import org.hit.chiikaiwabe.service.OtpService;
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
  private final PasswordEncoder passwordEncoder;

  private final OtpService otpService;

  private final StringRedisTemplate redisTemplate;

  private final ObjectMapper objectMapper;

  @Override
  public LoginResponseDto login(LoginRequestDto request) {
    try {
      User user = userRepository.findByEmail(request.getEmail())
              .orElseThrow(() -> new UnauthorizedException(ErrorMessage.Auth.ERR_INCORRECT_USERNAME));
      if (UserStatus.UNVERIFIED == user.getUserstatus()) {
        throw new UnauthorizedException(ErrorMessage.Auth.ERR_ACCOUNT_NOT_VERIFIED);
      }
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
      String accessToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.FALSE);
      String refreshToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.TRUE);

      long accessTtl = jwtTokenProvider.extractExpirationFromJwt(accessToken).getTime() - System.currentTimeMillis();
      long refreshTtl = jwtTokenProvider.extractExpirationFromJwt(refreshToken).getTime() - System.currentTimeMillis();

        redisTemplate.opsForValue().set(accessToken, userPrincipal.getId(), accessTtl, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set(refreshToken, userPrincipal.getId(), refreshTtl, TimeUnit.MILLISECONDS);

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

      if (!redisTemplate.hasKey(oldRefreshToken)) {
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


        redisTemplate.opsForValue().set(accessToken, userPrincipal.getId(), accessTtl, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set(refreshToken, userPrincipal.getId(), refreshTtl, TimeUnit.MILLISECONDS);

      return new TokenRefreshResponseDto(accessToken, refreshToken);

    } catch (Exception e) {
      throw new UnauthorizedException(ErrorMessage.Auth.INVALID_REFRESH_TOKEN);
    }
  }

  @Override
  public CommonResponseDto logout(HttpServletRequest request, String refreshToken) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      String accessToken = bearerToken.substring(7);
      long accessTtl = jwtTokenProvider.extractExpirationFromJwt(accessToken).getTime() - System.currentTimeMillis();
      if (accessTtl > 0) {
        redisTemplate.opsForValue().set("BLACKLIST:" + accessToken, "LOGGED_OUT", accessTtl, TimeUnit.MILLISECONDS);
      }
    }

    if (StringUtils.hasText(refreshToken)) {
      long refreshTtl = jwtTokenProvider.extractExpirationFromJwt(refreshToken).getTime() - System.currentTimeMillis();
      if (refreshTtl > 0) {
        redisTemplate.opsForValue().set("BLACKLIST:" + refreshToken, "LOGGED_OUT", refreshTtl, TimeUnit.MILLISECONDS);
      }
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

    try {
      String userJson = objectMapper.writeValueAsString(request);
      String redisKey = "TEMP_USER:" + request.getEmail();
      redisTemplate.opsForValue().set(redisKey, userJson, 5, TimeUnit.MINUTES);
    } catch (Exception e) {
      throw new RuntimeException(ErrorMessage.Auth.ERR_SYSTEM_PROCESS);
    }

    String otpCode = otpService.generateOtp(request.getEmail());
    otpService.sendOtp(request.getEmail(), otpCode);

    return new CommonResponseDto(true, SuccessMessage.REGISTER_SUCCESS_CHECK_EMAIL);
  }

  @Override
  public CommonResponseDto verifyRegisterOtp(VerifyOtpRequestDto request) {
    boolean isValid = otpService.validateOtp(request.getEmail(), request.getOtpCode());
    if (!isValid) {
      throw new InvalidException(ErrorMessage.Auth.ERR_OTP_INCORRECT);
    }
    String redisKey = "TEMP_USER:" + request.getEmail();
    String userJson = redisTemplate.opsForValue().get(redisKey);

    if (userJson == null) {
      throw new InvalidException(ErrorMessage.Auth.ERR_SESSION_EXPIRED);
    }

    try {
      UserCreateDto userDto = objectMapper.readValue(userJson, UserCreateDto.class);
      if (userRepository.existsByEmail(userDto.getEmail())) {
        throw new InvalidException(ErrorMessage.Auth.ERR_ACCOUNT_ALREADY_EXISTS);
      }
      int age = java.time.Period.between(userDto.getDateOfBirth(), java.time.LocalDate.now()).getYears();

      User newUser = User.builder()
              .username(userDto.getEmail())
              .email(userDto.getEmail())
              .password(passwordEncoder.encode(userDto.getPassword()))
              .firstName(userDto.getFirstName())
              .lastName(userDto.getLastName())
              .gender(userDto.getGender())
              .dateOfBirth(userDto.getDateOfBirth())
              .age(age)
              .userstatus(UserStatus.ACTIVE)
              .trustScore(100.0)
              .role(Role.USER)
              .build();

      userRepository.save(newUser);
      redisTemplate.delete(redisKey);

    } catch (Exception e) {
      throw new RuntimeException(ErrorMessage.Auth.ERR_SYSTEM_PROCESS);
    }
    return new CommonResponseDto(true, SuccessMessage.VERIFY_REGISTER_SUCCESS);
  }



  @Override
  public CommonResponseDto forgotPasswordSendOtp(SendOtpRequestDto request) {
    String spamKey = "OTP_RATE_LIMIT:" + request.getEmail();

    if (redisTemplate.hasKey(spamKey)) {
      throw new InvalidException(ErrorMessage.Auth.ERR_OTP_SPAM);
    }

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME,
                    new String[]{request.getEmail()}));
    if (user.getUserstatus() !=  UserStatus.ACTIVE) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_FORGOT_PASS_NOT_VERIFIED);
    }

    String otpCode = otpService.generateOtp(request.getEmail());
    otpService.sendOtp(request.getEmail(), otpCode);
    redisTemplate.opsForValue().set(spamKey, "BLOCKED", 60, TimeUnit.SECONDS);

    return new CommonResponseDto(true, SuccessMessage.FORGOT_PASSWORD_SEND_OTP_SUCCESS);
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
    if (!redisTemplate.hasKey(resetTicketKey)) {
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
