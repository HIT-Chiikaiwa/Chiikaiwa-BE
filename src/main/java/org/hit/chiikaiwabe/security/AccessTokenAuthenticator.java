package org.hit.chiikaiwabe.security;

import lombok.RequiredArgsConstructor;
import org.hit.chiikaiwabe.constant.ErrorMessage;
import org.hit.chiikaiwabe.exception.UnauthorizedException;
import org.hit.chiikaiwabe.security.jwt.JwtTokenProvider;
import org.hit.chiikaiwabe.service.CustomUserDetailsService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessTokenAuthenticator {
    private final JwtTokenProvider tokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final CustomUserDetailsService customUserDetailsService;

    public UserPrincipal authenticate(String token) {
        if (!tokenProvider.validateAccessToken(token)
                || Boolean.TRUE.equals(redisTemplate.hasKey("BLACKLIST:" + tokenProvider.extractJtiFromJwt(token)))) {
            throw new UnauthorizedException(ErrorMessage.UNAUTHORIZED);
        }
        return (UserPrincipal) customUserDetailsService.loadUserById(tokenProvider.extractSubjectFromJwt(token));
    }
}
