package org.hit.chiikaiwabe.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.domain.enums.Role;
import org.hit.chiikaiwabe.domain.enums.UserStatus;
import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.CompleteProfileResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.GoogleLoginResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.LoginResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.TokenRefreshResponseDto;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.AuthProvider;
import org.hit.chiikaiwabe.exception.InternalServerException;
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

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.hit.chiikaiwabe.service.FirebaseService;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

  private final FirebaseService firebaseService;

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

      String accessJti = jwtTokenProvider.extractJtiFromJwt(accessToken);
      String refreshJti = jwtTokenProvider.extractJtiFromJwt(refreshToken);

      redisTemplate.opsForValue().set(accessJti, userPrincipal.getId(), accessTtl, TimeUnit.MILLISECONDS);
      redisTemplate.opsForValue().set(refreshJti, userPrincipal.getId(), refreshTtl, TimeUnit.MILLISECONDS);

      return new LoginResponseDto(accessToken, refreshToken, userPrincipal.getId(), authentication.getAuthorities());
    } catch (InternalAuthenticationServiceException e) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_INCORRECT_USERNAME);
    } catch (BadCredentialsException e) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_INCORRECT_PASSWORD);
    }
  }

  @Override
  public GoogleLoginResponseDto loginWithGoogle(GoogleLoginRequestDto request) {
    try {
      FirebaseToken firebaseToken = firebaseService.verifyIdToken(request.getIdToken());
      String email = firebaseToken.getEmail();
      String uid = firebaseToken.getUid();

      Optional<User> existingUser = userRepository.findByEmail(email);

      if (existingUser.isPresent()) {
        User user = existingUser.get();

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.FALSE);
        String refreshToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.TRUE);

        long accessTtl = jwtTokenProvider.extractExpirationFromJwt(accessToken).getTime() - System.currentTimeMillis();
        long refreshTtl = jwtTokenProvider.extractExpirationFromJwt(refreshToken).getTime() - System.currentTimeMillis();

        String accessJti = jwtTokenProvider.extractJtiFromJwt(accessToken);
        String refreshJti = jwtTokenProvider.extractJtiFromJwt(refreshToken);

        redisTemplate.opsForValue().set(accessJti, userPrincipal.getId(), accessTtl, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set(refreshJti, userPrincipal.getId(), refreshTtl, TimeUnit.MILLISECONDS);

        return new GoogleLoginResponseDto(email, accessToken, refreshToken);
      } else {
        String ticket = java.util.UUID.randomUUID().toString();
        GoogleRegistrationTempData tempData = new GoogleRegistrationTempData(email, uid);
        String redisKey = "google_reg:" + ticket;
        redisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(tempData), 15, TimeUnit.MINUTES);

        return new GoogleLoginResponseDto(email, ticket);
      }
    } catch (Exception e) {
      log.error("Google login error", e);
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_FIREBASE_TOKEN_INVALID);
    }
  }

  @Override
  public CompleteProfileResponseDto completeGoogleProfile(CompleteProfileRequestDto request) {
    String redisKey = "google_reg:" + request.getTicket();
    String jsonStr = redisTemplate.opsForValue().get(redisKey);

    if (jsonStr == null) {
      throw new InvalidException(ErrorMessage.Auth.ERR_SESSION_EXPIRED);
    }

    try {
      GoogleRegistrationTempData tempData = objectMapper.readValue(jsonStr, GoogleRegistrationTempData.class);

      if (userRepository.existsByEmail(tempData.getEmail())) {
        throw new InvalidException(ErrorMessage.Auth.ERR_ACCOUNT_ALREADY_EXISTS);
      }

      String randomPassword = java.util.UUID.randomUUID().toString();

      User newUser = User.builder()
              .username(tempData.getEmail())
              .email(tempData.getEmail())
              .password(passwordEncoder.encode(randomPassword))
              .firstName(request.getFirstName())
              .lastName(request.getLastName())
              .gender(request.getGender())
              .dateOfBirth(request.getDateOfBirth())
              .age(Period.between(request.getDateOfBirth(), LocalDate.now()).getYears())
              .authProvider(AuthProvider.GOOGLE)
              .providerId(tempData.getProviderId())
              .userstatus(UserStatus.ACTIVE)
              .trustScore(100.0)
              .role(Role.USER)
              .build();

      userRepository.save(newUser);
      redisTemplate.delete(redisKey);

      UserPrincipal userPrincipal = UserPrincipal.create(newUser);
      String accessToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.FALSE);
      String refreshToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.TRUE);

      long accessTtl = jwtTokenProvider.extractExpirationFromJwt(accessToken).getTime() - System.currentTimeMillis();
      long refreshTtl = jwtTokenProvider.extractExpirationFromJwt(refreshToken).getTime() - System.currentTimeMillis();

      String accessJti = jwtTokenProvider.extractJtiFromJwt(accessToken);
      String refreshJti = jwtTokenProvider.extractJtiFromJwt(refreshToken);

      redisTemplate.opsForValue().set(accessJti, userPrincipal.getId(), accessTtl, TimeUnit.MILLISECONDS);
      redisTemplate.opsForValue().set(refreshJti, userPrincipal.getId(), refreshTtl, TimeUnit.MILLISECONDS);

      return new CompleteProfileResponseDto("Bearer", accessToken, refreshToken, newUser.getId(), true);

    } catch (Exception e) {
      log.error("Complete profile error", e);
      throw new InternalServerException(ErrorMessage.Auth.ERR_SYSTEM_PROCESS);
    }
  }

  @Override
  public TokenRefreshResponseDto refresh(TokenRefreshRequestDto request) {
    try {
      String oldRefreshToken = request.getRefreshToken();
      String oldJti = jwtTokenProvider.extractJtiFromJwt(oldRefreshToken);

      if (!Boolean.TRUE.equals(redisTemplate.hasKey(oldJti))) {
        throw new UnauthorizedException(ErrorMessage.Auth.INVALID_REFRESH_TOKEN);
      }
      Authentication authentication = jwtTokenProvider.getAuthenticationByRefreshToken(oldRefreshToken);
      String identifier = authentication.getName();
      UserPrincipal userPrincipal = (UserPrincipal) customUserDetailsService.loadUserByUsername(identifier);
      String accessToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.FALSE);
      String refreshToken = jwtTokenProvider.generateToken(userPrincipal, Boolean.TRUE);

      redisTemplate.delete(oldJti);
      long accessTtl = jwtTokenProvider.extractExpirationFromJwt(accessToken).getTime() - System.currentTimeMillis();
      long refreshTtl = jwtTokenProvider.extractExpirationFromJwt(refreshToken).getTime() - System.currentTimeMillis();

      String accessJti = jwtTokenProvider.extractJtiFromJwt(accessToken);
      String refreshJti = jwtTokenProvider.extractJtiFromJwt(refreshToken);

      redisTemplate.opsForValue().set(accessJti, userPrincipal.getId(), accessTtl, TimeUnit.MILLISECONDS);
      redisTemplate.opsForValue().set(refreshJti, userPrincipal.getId(), refreshTtl, TimeUnit.MILLISECONDS);

      return new TokenRefreshResponseDto(accessToken, refreshToken);

    } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
      throw new UnauthorizedException(ErrorMessage.Auth.INVALID_REFRESH_TOKEN);
    }
  }

  @Override
  public void logout(HttpServletRequest request, String refreshToken) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      String accessToken = bearerToken.substring(7);
      try {
        String accessJti = jwtTokenProvider.extractJtiFromJwt(accessToken);
        long accessTtl = jwtTokenProvider.extractExpirationFromJwt(accessToken).getTime() - System.currentTimeMillis();
        if (accessTtl > 0) {
          redisTemplate.opsForValue().set("BLACKLIST:" + accessJti, "LOGGED_OUT", accessTtl, TimeUnit.MILLISECONDS);
        }
      } catch (Exception e) {
        log.warn("Invalid access token on logout", e);
      }
    }

    if (StringUtils.hasText(refreshToken)) {
      try {
        String refreshJti = jwtTokenProvider.extractJtiFromJwt(refreshToken);
        long refreshTtl = jwtTokenProvider.extractExpirationFromJwt(refreshToken).getTime() - System.currentTimeMillis();
        if (refreshTtl > 0) {
          redisTemplate.opsForValue().set("BLACKLIST:" + refreshJti, "LOGGED_OUT", refreshTtl, TimeUnit.MILLISECONDS);
        }
      } catch (Exception e) {
        log.warn("Invalid refresh token on logout", e);
      }
    }

    SecurityContextHolder.clearContext();
  }


  @Override
  public void register(UserCreateDto request) {
    if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
      throw new InvalidException("Email không hợp lệ");
    }
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
      log.error("Failed to register user to Redis", e);
      throw new InternalServerException(ErrorMessage.Auth.ERR_SYSTEM_PROCESS);
    }

    String otpCode = otpService.generateOtp(request.getEmail());
    otpService.sendOtp(request.getEmail(), otpCode);
  }

  @Override
  public void verifyRegisterOtp(VerifyOtpRequestDto request) {
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
      log.error("Failed to create user on verify OTP", e);
      throw new InternalServerException(ErrorMessage.Auth.ERR_SYSTEM_PROCESS);
    }
  }



  @Override
  public void forgotPasswordSendOtp(SendOtpRequestDto request) {
    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME,
                    new String[]{request.getEmail()}));

    if (user.getAuthProvider() == AuthProvider.GOOGLE) {
      throw new InvalidException(ErrorMessage.Auth.ERR_GOOGLE_USER_NO_PASSWORD);
    }

    if (user.getUserstatus() !=  UserStatus.ACTIVE) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_FORGOT_PASS_NOT_VERIFIED);
    }

    String otpCode = otpService.generateOtp(request.getEmail());
    otpService.sendOtp(request.getEmail(), otpCode);
  }
  @Override
  public void verifyForgotPasswordOtp(VerifyOtpRequestDto request) {
    boolean isValid = otpService.validateOtp(request.getEmail(), request.getOtpCode());
    if (!isValid) {
      throw new InvalidException(ErrorMessage.Auth.ERR_OTP_INCORRECT);
    }
    String resetTicketKey = "RESET_TICKET:" + request.getEmail();
    redisTemplate.opsForValue().set(resetTicketKey, "VALID", 5, TimeUnit.MINUTES);
  }

  @Override
  public void resetPassword(ResetPasswordRequestDto request) {
    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new NotFoundException(ErrorMessage.User.ERR_NOT_FOUND_USERNAME, new String[]{request.getEmail()}));

    if (user.getAuthProvider() == AuthProvider.GOOGLE) {
      throw new InvalidException(ErrorMessage.Auth.ERR_GOOGLE_USER_NO_PASSWORD);
    }

    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new InvalidException(ErrorMessage.Auth.ERR_CONFIRM_PASSWORD_NOT_MATCH);
    }
    String resetTicketKey = "RESET_TICKET:" + request.getEmail();
    if (!redisTemplate.hasKey(resetTicketKey)) {
      throw new UnauthorizedException(ErrorMessage.Auth.ERR_RESET_TICKET_EXPIRED);
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
    redisTemplate.delete(resetTicketKey);
  }

}
