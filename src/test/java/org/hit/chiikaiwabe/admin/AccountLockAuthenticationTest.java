package org.hit.chiikaiwabe.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseToken;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.domain.dto.request.*;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.*;
import org.hit.chiikaiwabe.exception.UnauthorizedException;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.security.UserPrincipal;
import org.hit.chiikaiwabe.security.jwt.*;
import org.hit.chiikaiwabe.service.*;
import org.hit.chiikaiwabe.service.impl.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.FilterChain;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountLockAuthenticationTest {
  @Mock AuthenticationManager authenticationManager;
  @Mock JwtTokenProvider tokenProvider;
  @Mock UserRepository repository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock OtpService otpService;
  @Mock StringRedisTemplate redis;
  @Mock ObjectMapper objectMapper;
  @Mock FirebaseService firebase;
  @Mock ValueOperations<String, String> values;
  @Mock org.hit.chiikaiwabe.security.RefreshTokenStore refreshTokenStore;
  CustomUserDetailsServiceImpl details;
  AuthServiceImpl auth;
  User user;

  @BeforeEach void setup() {
    details = new CustomUserDetailsServiceImpl(repository);
    auth = new AuthServiceImpl(authenticationManager, tokenProvider, details, repository,
        passwordEncoder, otpService, redis, objectMapper, firebase, refreshTokenStore);
    user = User.builder().id("user-1").username("alice@example.com").email("alice@example.com")
        .role(Role.USER).userstatus(UserStatus.LOCKED).build();
  }

  @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

  @Test void lockedAccountCannotLogInWithPassword() {
    when(repository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    LoginRequestDto request = new LoginRequestDto(user.getEmail(), "password");
    assertThatThrownBy(() -> auth.login(request)).isInstanceOf(UnauthorizedException.class)
        .hasMessage(ErrorMessage.Auth.ERR_ACCOUNT_LOCKED);
    verifyNoInteractions(authenticationManager, tokenProvider);
  }

  @Test void seededAdminCanLogInUsingUsernameInExistingEmailField() {
    user.setUsername("admin1");
    user.setRole(Role.ADMIN);
    user.setUserstatus(UserStatus.ACTIVE);
    when(repository.findByEmail("admin1")).thenReturn(Optional.empty());
    when(repository.findByUsername("admin1")).thenReturn(Optional.of(user));
    UserPrincipal principal = UserPrincipal.create(user);
    when(authenticationManager.authenticate(any())).thenReturn(
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    when(tokenProvider.generateToken(principal, false)).thenReturn("access");
    when(tokenProvider.generateToken(principal, true)).thenReturn("refresh");
    when(tokenProvider.extractExpirationFromJwt(anyString()))
        .thenReturn(new java.util.Date(System.currentTimeMillis() + 60000));
    when(tokenProvider.extractJtiFromJwt("access")).thenReturn("access-jti");
    when(tokenProvider.extractJtiFromJwt("refresh")).thenReturn("refresh-jti");
    when(redis.opsForValue()).thenReturn(values);
    assertThat(auth.login(new LoginRequestDto("admin1", "password")).getAccessToken()).isEqualTo("access");
  }

  @Test void googleLoginReturnsLockedErrorInsteadOfInternalServerError() throws Exception {
    FirebaseToken token = mock(FirebaseToken.class);
    when(firebase.verifyIdToken("firebase-token")).thenReturn(token);
    when(token.getEmail()).thenReturn(user.getEmail());
    when(token.getUid()).thenReturn("google-id");
    user.setAuthProvider(AuthProvider.GOOGLE);
    user.setProviderId("google-id");
    when(repository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    GoogleLoginRequestDto request = new GoogleLoginRequestDto();
    request.setIdToken("firebase-token");
    assertThatThrownBy(() -> auth.loginWithGoogle(request)).isInstanceOf(UnauthorizedException.class)
        .hasMessage(ErrorMessage.Auth.ERR_ACCOUNT_LOCKED);
    verifyNoInteractions(tokenProvider);
  }

  @Test void lockedAccountCannotRefreshExistingToken() {
    when(tokenProvider.extractJtiFromJwt("refresh")).thenReturn("jti");
    when(tokenProvider.getAuthenticationByRefreshToken("refresh")).thenReturn(
        new UsernamePasswordAuthenticationToken(user.getUsername(), null));
    when(repository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    TokenRefreshRequestDto request = new TokenRefreshRequestDto("refresh");
    assertThatThrownBy(() -> auth.refresh(request)).isInstanceOf(UnauthorizedException.class)
        .hasMessage(ErrorMessage.Auth.ERR_ACCOUNT_LOCKED);
    verify(tokenProvider, never()).generateToken(any(), anyBoolean());
  }

  @Test void existingAccessTokenStopsAuthenticatingAfterLockAndWorksAfterUnlock() throws Exception {
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
        new org.hit.chiikaiwabe.security.AccessTokenAuthenticator(tokenProvider, redis, details));
    when(tokenProvider.validateAccessToken("access")).thenReturn(true);
    when(tokenProvider.extractJtiFromJwt("access")).thenReturn("jti");
    when(tokenProvider.extractSubjectFromJwt("access")).thenReturn(user.getId());
    when(repository.findById(user.getId())).thenReturn(Optional.of(user));
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer access");
    FilterChain chain = mock(FilterChain.class);
    filter.doFilter(request, new MockHttpServletResponse(), chain);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    user.setUserstatus(UserStatus.ACTIVE);
    filter.doFilter(request, new MockHttpServletResponse(), chain);
    assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated()).isTrue();
  }

  @Test void deletedAndUnverifiedAccountsCannotUseExistingTokens() {
    when(repository.findById(user.getId())).thenReturn(Optional.of(user));
    user.setUserstatus(UserStatus.ACTIVE);
    user.setDeleteFlag(true);
    assertThatThrownBy(() -> details.loadUserById(user.getId())).isInstanceOf(UnauthorizedException.class)
        .hasMessage(ErrorMessage.Auth.ERR_ACCOUNT_NOT_ENABLED);
    user.setDeleteFlag(false);
    user.setUserstatus(UserStatus.UNVERIFIED);
    assertThatThrownBy(() -> details.loadUserById(user.getId())).isInstanceOf(UnauthorizedException.class)
        .hasMessage(ErrorMessage.Auth.ERR_ACCOUNT_NOT_VERIFIED);
  }

  @Test void principalExposesLockAndEnableFlags() {
    assertThat(UserPrincipal.create(user).isAccountNonLocked()).isFalse();
    user.setUserstatus(UserStatus.ACTIVE);
    assertThat(UserPrincipal.create(user).isAccountNonLocked()).isTrue();
    assertThat(UserPrincipal.create(user).isEnabled()).isTrue();
    user.setDeleteFlag(true);
    assertThat(UserPrincipal.create(user).isEnabled()).isFalse();
  }
}
