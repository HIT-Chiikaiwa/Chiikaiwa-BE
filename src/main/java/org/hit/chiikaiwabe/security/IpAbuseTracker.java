package org.hit.chiikaiwabe.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class IpAbuseTracker {

    private static final String ABUSE_KEY_PREFIX = "IP_ABUSE:";
    private static final String BANNED_KEY_PREFIX = "IP_BANNED:";

    private static final long VIOLATION_THRESHOLD = 50;
    private static final long ABUSE_WINDOW_MINUTES = 10;
    private static final long BAN_DURATION_MINUTES = 30;

    private final StringRedisTemplate redisTemplate;

    public void recordViolation(String clientIp) {
        String abuseKey = ABUSE_KEY_PREFIX + clientIp;

        Long count = redisTemplate.opsForValue().increment(abuseKey);
        if (count != null && count == 1) {
            redisTemplate.expire(abuseKey, ABUSE_WINDOW_MINUTES, TimeUnit.MINUTES);
        }

        if (count != null && count >= VIOLATION_THRESHOLD) {
            String banKey = BANNED_KEY_PREFIX + clientIp;
            redisTemplate.opsForValue().set(banKey, "BANNED", BAN_DURATION_MINUTES, TimeUnit.MINUTES);
            log.error("IP {} has been temporarily banned for {} minutes after {} rate limit violations",
                    clientIp, BAN_DURATION_MINUTES, count);
        }
    }

    public boolean isBanned(String clientIp) {
        String banKey = BANNED_KEY_PREFIX + clientIp;
        return Boolean.TRUE.equals(redisTemplate.hasKey(banKey));
    }
}
