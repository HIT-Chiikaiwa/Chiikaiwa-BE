package org.hit.chiikaiwabe.service;

import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.dto.response.CommonResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.CompleteProfileResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.GoogleLoginResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.LoginResponseDto;
import org.hit.chiikaiwabe.domain.dto.response.TokenRefreshResponseDto;

import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

  LoginResponseDto login(LoginRequestDto request);

  GoogleLoginResponseDto loginWithGoogle(GoogleLoginRequestDto request);

  CompleteProfileResponseDto completeGoogleProfile(CompleteProfileRequestDto request);

  TokenRefreshResponseDto refresh(TokenRefreshRequestDto request);

  void logout(HttpServletRequest request, String refreshToken);

  void register(UserCreateDto request);

  void verifyRegisterOtp(VerifyOtpRequestDto request);

  void forgotPasswordSendOtp(SendOtpRequestDto request);

  void verifyForgotPasswordOtp(VerifyOtpRequestDto request);

  void resetPassword(ResetPasswordRequestDto request);

}
