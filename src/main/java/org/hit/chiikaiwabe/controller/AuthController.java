package org.hit.chiikaiwabe.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.hit.chiikaiwabe.base.RestApiV1;
import org.hit.chiikaiwabe.base.VsResponseUtil;
import org.hit.chiikaiwabe.constant.SuccessMessage;
import org.hit.chiikaiwabe.constant.UrlConstant;
import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.service.AuthService;
import org.hit.chiikaiwabe.service.OtpService;
import org.hit.chiikaiwabe.validator.annotation.ValidFileImage;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.annotation.RateLimit;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@RequiredArgsConstructor
@Validated
@RestApiV1
public class AuthController {

  private final AuthService authService;
  private final OtpService otpService;

  @Operation(summary = "API Login")
  @PostMapping(UrlConstant.Auth.LOGIN)
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
    return VsResponseUtil.success(authService.login(request));
  }

  @Operation(summary = "API test")
  @PostMapping("auth/test")
  public ResponseEntity<?> login(@ValidFileImage MultipartFile multipartFile) {
    return VsResponseUtil.success(multipartFile.getContentType());
  }

  @Operation(summary = "API Google Login/Register")
  @PostMapping(UrlConstant.Auth.GOOGLE_LOGIN)
  public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequestDto request) {
    return VsResponseUtil.success(authService.loginWithGoogle(request));
  }

  @Operation(summary = "API Complete Profile (Google registration)")
  @PostMapping(UrlConstant.Auth.COMPLETE_PROFILE)
  public ResponseEntity<?> completeProfile(@Valid @RequestBody CompleteProfileRequestDto request) {
    return VsResponseUtil.success(authService.completeGoogleProfile(request));
  }

  @Operation(summary = "API Refresh Token")
  @PostMapping(UrlConstant.Auth.REFRESH_TOKEN)
  public ResponseEntity<?> refresh(@Valid @RequestBody TokenRefreshRequestDto request) {
    return VsResponseUtil.success(authService.refresh(request));
  }

  @Operation(summary = "API Logout")
  @PostMapping(UrlConstant.Auth.LOGOUT)
  public ResponseEntity<?> logout(HttpServletRequest request, @Valid @RequestBody TokenRefreshRequestDto tokenRequest) {
    authService.logout(request, tokenRequest.getRefreshToken());
    return VsResponseUtil.success(new CommonResponseDto(true, SuccessMessage.LOGOUT_SUCCESS));
  }

  @Operation(summary = "API Register")
  @PostMapping(UrlConstant.Auth.REGISTER)
  public ResponseEntity<?> register(@Valid @RequestBody UserCreateDto request) {
    authService.register(request);
    return VsResponseUtil.success(new CommonResponseDto(true, SuccessMessage.REGISTER_SUCCESS_CHECK_EMAIL));
  }

  @Operation(summary = "API Send OTP")
  @RateLimit(capacity = 1, durationInSeconds = 30)
  @PostMapping(UrlConstant.Auth.SEND_OTP)
  public ResponseEntity<?> sendOtp(@Valid @RequestBody SendOtpRequestDto request) {
    String otpCode = otpService.generateOtp(request.getEmail());
    otpService.sendOtp(request.getEmail(), otpCode);
    return VsResponseUtil.success(
            new CommonResponseDto(true, SuccessMessage.SEND_OTP_SUCCESS)
    );
  }

  @Operation(summary = "API Verify Register OTP")
  @PostMapping(UrlConstant.Auth.VERIFY_REGISTER_OTP)
  public ResponseEntity<?> verifyRegisterOtp(@Valid @RequestBody VerifyOtpRequestDto request) {
    authService.verifyRegisterOtp(request);
    return VsResponseUtil.success(new CommonResponseDto(true, SuccessMessage.VERIFY_REGISTER_SUCCESS));
  }


  @Operation(summary = "API Forgot Password Send OTP")
  @RateLimit(capacity = 1, durationInSeconds = 30)
  @PostMapping(UrlConstant.Auth.FORGOT_PASSWORD_SEND_OTP)
  public ResponseEntity<?> forgotPasswordSendOtp(@Valid @RequestBody SendOtpRequestDto request) {
    authService.forgotPasswordSendOtp(request);
    return VsResponseUtil.success(new CommonResponseDto(true, SuccessMessage.FORGOT_PASSWORD_SEND_OTP_SUCCESS));
  }

  @Operation(summary = "API Verify Forgot Password OTP")
  @PostMapping(UrlConstant.Auth.FORGOT_PASSWORD_VERIFY_OTP)
  public ResponseEntity<?> verifyForgotPasswordOtp(@Valid @RequestBody VerifyOtpRequestDto request) {
    authService.verifyForgotPasswordOtp(request);
    return VsResponseUtil.success(new CommonResponseDto(true, SuccessMessage.VERIFY_OTP_SUCCESS));
  }

  @Operation(summary = "API Reset Password")
  @PostMapping(UrlConstant.Auth.FORGOT_PASSWORD_RESET)
  public ResponseEntity<?> resetPassword(
          @Valid @RequestBody ResetPasswordRequestDto request) {
    authService.resetPassword(request);
    return VsResponseUtil.success(new CommonResponseDto(true, SuccessMessage.RESET_PASSWORD_SUCCESS));
  }

}
