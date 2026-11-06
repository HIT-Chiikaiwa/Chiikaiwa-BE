package org.hit.chiikaiwabe.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hit.chiikaiwabe.domain.dto.request.TokenRefreshRequestDto;
import org.hit.chiikaiwabe.domain.dto.response.TokenRefreshResponseDto;
import org.hit.chiikaiwabe.domain.entity.User;
import org.hit.chiikaiwabe.domain.enums.*;
import org.hit.chiikaiwabe.exception.UnauthorizedException;
import org.hit.chiikaiwabe.repository.UserRepository;
import org.hit.chiikaiwabe.security.*;
import org.hit.chiikaiwabe.security.jwt.JwtTokenProvider;
import org.hit.chiikaiwabe.service.*;
import org.hit.chiikaiwabe.service.impl.*;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Run against a disposable Redis: -Dtest.redis.port=<mapped local port>. */
class TokenLifecycleRedisTest {
    static JedisConnectionFactory factory;
    static StringRedisTemplate redis;
    JwtTokenProvider tokens;
    AuthServiceImpl auth;
    User user;
    CustomUserDetailsServiceImpl details;
    Set<String> cleanup = ConcurrentHashMap.newKeySet();

    @BeforeAll static void redisConnection() {
        String port = System.getProperty("test.redis.port");
        Assumptions.assumeTrue(port != null, "Requires disposable Redis via -Dtest.redis.port");
        factory = new JedisConnectionFactory(new RedisStandaloneConfiguration("127.0.0.1", Integer.parseInt(port)));
        factory.afterPropertiesSet();
        redis = new StringRedisTemplate(factory);
    }

    @AfterAll static void disconnect() { if (factory != null) factory.destroy(); }

    @BeforeEach void setup() {
        tokens = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokens, "SECRET_KEY", "Y2hpaWlrYWl3YS10ZXN0LXNlY3JldC1rZXktZm9yLWp3dC1vbmx5");
        ReflectionTestUtils.setField(tokens, "EXPIRATION_TIME_ACCESS_TOKEN", 30);
        ReflectionTestUtils.setField(tokens, "EXPIRATION_TIME_REFRESH_TOKEN", 60);
        user = User.builder().id(UUID.randomUUID().toString()).username("token-test-" + UUID.randomUUID())
                .role(Role.USER).userstatus(UserStatus.ACTIVE).build();
        UserRepository repository = mock(UserRepository.class);
        when(repository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(repository.findById(user.getId())).thenReturn(Optional.of(user));
        details = new CustomUserDetailsServiceImpl(repository);
        auth = new AuthServiceImpl(mock(AuthenticationManager.class), tokens, details,
                repository, mock(PasswordEncoder.class), mock(OtpService.class), redis, new ObjectMapper(),
                mock(FirebaseService.class), new RefreshTokenStore(redis));
    }

    @AfterEach void cleanup() { if (!cleanup.isEmpty()) redis.delete(cleanup); }

    String issue(boolean refresh) {
        String token = tokens.generateToken(UserPrincipal.create(user), refresh);
        track(token);
        redis.opsForValue().set(tokens.extractJtiFromJwt(token), user.getId(), Duration.ofMinutes(5));
        return token;
    }

    void track(String token) {
        String jti = tokens.extractJtiFromJwt(token);
        cleanup.add(jti); cleanup.add("BLACKLIST:" + jti);
    }

    @Test void logoutRevokesBothTokensAndRefreshCannotBeReused() {
        String access = issue(false), refresh = issue(true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + access);
        var authenticator = new AccessTokenAuthenticator(tokens, redis, details);
        assertThat(authenticator.authenticate(access).getId()).isEqualTo(user.getId());
        auth.logout(request, refresh);
        assertThatThrownBy(() -> authenticator.authenticate(access)).isInstanceOf(UnauthorizedException.class);
        assertThat(redis.hasKey(tokens.extractJtiFromJwt(refresh))).isFalse();
        assertThat(redis.hasKey("BLACKLIST:" + tokens.extractJtiFromJwt(refresh))).isTrue();
        assertThat(redis.hasKey("BLACKLIST:" + tokens.extractJtiFromJwt(access))).isTrue();
        assertThatThrownBy(() -> auth.refresh(new TokenRefreshRequestDto(refresh))).isInstanceOf(UnauthorizedException.class);
    }

    @Test void oldBlacklistEntriesCannotRefreshEvenIfLegacyActiveKeyExists() {
        String refresh = issue(true);
        redis.opsForValue().set("BLACKLIST:" + tokens.extractJtiFromJwt(refresh), "LOGGED_OUT", Duration.ofMinutes(5));
        assertThatThrownBy(() -> auth.refresh(new TokenRefreshRequestDto(refresh))).isInstanceOf(UnauthorizedException.class);
    }

    @Test void concurrentRefreshOnlyIssuesOneNewPair() throws Exception {
        String refresh = issue(true);
        var executor = Executors.newFixedThreadPool(8);
        var start = new CountDownLatch(1);
        try {
            List<Future<TokenRefreshResponseDto>> results = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                results.add(executor.submit(() -> {
                    start.await();
                    try { return auth.refresh(new TokenRefreshRequestDto(refresh)); }
                    catch (UnauthorizedException ex) { return null; }
                }));
            }
            start.countDown();
            int success = 0;
            TokenRefreshResponseDto winner = null;
            for (var result : results) {
                var pair = result.get(15, TimeUnit.SECONDS);
                if (pair != null) {
                    success++; winner = pair; track(pair.getAccessToken()); track(pair.getRefreshToken());
                }
            }
            assertThat(success).isEqualTo(1);
            assertThat(tokens.validateAccessToken(winner.getAccessToken())).isTrue();
            assertThat(redis.hasKey(tokens.extractJtiFromJwt(refresh))).isFalse();
            assertThatThrownBy(() -> auth.refresh(new TokenRefreshRequestDto(refresh))).isInstanceOf(UnauthorizedException.class);
        } finally { executor.shutdownNow(); }
    }

    @Test void refreshRequiresRefreshTypeAndCorrectActiveTokenOwner() {
        String access = issue(false);
        assertThatThrownBy(() -> auth.refresh(new TokenRefreshRequestDto(access))).isInstanceOf(UnauthorizedException.class);
        String refresh = issue(true);
        redis.opsForValue().set(tokens.extractJtiFromJwt(refresh), "other-owner", Duration.ofMinutes(5));
        assertThatThrownBy(() -> auth.refresh(new TokenRefreshRequestDto(refresh))).isInstanceOf(UnauthorizedException.class);
    }

    @Test void cannotLogoutAnotherUsersRefreshTokenWithOwnAccessToken() {
        String refresh = issue(true);
        User other = User.builder().id("other").username("other").role(Role.USER).userstatus(UserStatus.ACTIVE).build();
        String access = tokens.generateToken(UserPrincipal.create(other), false);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + access);
        assertThatThrownBy(() -> auth.logout(request, refresh)).isInstanceOf(UnauthorizedException.class);
        assertThat(redis.hasKey(tokens.extractJtiFromJwt(refresh))).isTrue();
    }
}
