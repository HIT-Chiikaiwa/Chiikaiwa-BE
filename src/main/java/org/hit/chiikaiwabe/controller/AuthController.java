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
  public String login(@ValidFileImage MultipartFile multipartFile) {
    return multipartFile.getContentType();
  }

  @Operation(summary = "API Refresh Token")
  @PostMapping(UrlConstant.Auth.REFRESH_TOKEN)
  public ResponseEntity<?> refresh(@Valid @RequestBody TokenRefreshRequestDto request) {
    return VsResponseUtil.success(authService.refresh(request));
  }

  @Operation(summary = "API Logout")
  @PostMapping(UrlConstant.Auth.LOGOUT)
  public ResponseEntity<?> logout(HttpServletRequest request, @Valid @RequestBody TokenRefreshRequestDto tokenRequest) {
    return VsResponseUtil.success(authService.logout(request, tokenRequest.getRefreshToken()));
  }

  @Operation(summary = "API Register")
  @PostMapping(UrlConstant.Auth.REGISTER)
  public ResponseEntity<?> register(@RequestBody UserCreateDto request) {
    return VsResponseUtil.success(authService.register(request));
  }

  @Operation(summary = "API Send OTP")
  @PostMapping(UrlConstant.Auth.SEND_OTP)
  public ResponseEntity<?> sendOtp(@RequestBody SendOtpRequestDto request ){
    otpService.generateAndSendOtp(request.getEmail());

    return VsResponseUtil.success(
            new CommonResponseDto(true, SuccessMessage.SEND_OTP_SUCCESS)
    );
  }

  @Operation(summary = "API Verify Register OTP")
  @PostMapping(UrlConstant.Auth.VERIFY_REGISTER_OTP)
  public ResponseEntity<?> verifyRegisterOtp(@Valid @RequestBody VerifyOtpRequestDto request) {
    return VsResponseUtil.success(authService.verifyRegisterOtp(request));
  }


  @Operation(summary = "API Forgot Password Send OTP")
  @PostMapping(UrlConstant.Auth.FORGOT_PASSWORD_SEND_OTP)
  public ResponseEntity<?> forgotPasswordSendOtp(@Valid @RequestBody SendOtpRequestDto request) {
    return VsResponseUtil.success(authService.forgotPasswordSendOtp(request));
  }

  @Operation(summary = "API Verify Forgot Password OTP")
  @PostMapping(UrlConstant.Auth.FORGOT_PASSWORD_VERIFY_OTP)
  public ResponseEntity<?> verifyForgotPasswordOtp(@Valid @RequestBody VerifyOtpRequestDto request) {
    return VsResponseUtil.success(authService.verifyForgotPasswordOtp(request));
  }

  @Operation(summary = "API Reset Password")
  @PostMapping(UrlConstant.Auth.FORGOT_PASSWORD_RESET)
  public ResponseEntity<?> resetPassword(
          @Valid @RequestBody ResetPasswordRequestDto request) {
    return VsResponseUtil.success(authService.resetPassword(request));
  }

}
