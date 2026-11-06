package org.hit.chiikaiwabe.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {
    private final StringRedisTemplate redisTemplate;

    // Check revocation and ownership, then consume in a single Redis operation.
    private static final DefaultRedisScript<Long> CONSUME = new DefaultRedisScript<>(
            "if redis.call('EXISTS', KEYS[2]) == 1 then return 0 end " +
            "if redis.call('GET', KEYS[1]) ~= ARGV[1] then return 0 end " +
            "return redis.call('DEL', KEYS[1])", Long.class);
    private static final DefaultRedisScript<Long> REVOKE = new DefaultRedisScript<>(
            "redis.call('SET', KEYS[2], 'LOGGED_OUT', 'PX', ARGV[1]); " +
            "return redis.call('DEL', KEYS[1])", Long.class);

    public boolean consume(String jti, String userId) {
        return Long.valueOf(1).equals(redisTemplate.execute(CONSUME,
                List.of(jti, "BLACKLIST:" + jti), userId));
    }

    public void revoke(String jti, long ttlMillis) {
        if (ttlMillis > 0) {
            redisTemplate.execute(REVOKE, List.of(jti, "BLACKLIST:" + jti), Long.toString(ttlMillis));
        }
    }
}
