package org.hit.chiikaiwabe.interceptor;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hit.chiikaiwabe.annotation.RateLimit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ProxyManager<byte[]> proxyManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);

        if (rateLimit == null) {
            rateLimit = handlerMethod.getBeanType().getAnnotation(RateLimit.class);
        }

        long capacity = rateLimit != null ? rateLimit.capacity() : 200;
        long durationInSeconds = rateLimit != null ? rateLimit.durationInSeconds() : 60;

        String clientIp = getClientIP(request);
        String requestUri = request.getRequestURI();

        String bucketKeyString = "RATE_LIMIT:" + clientIp + ":" + requestUri;
        byte[] bucketKey = bucketKeyString.getBytes(StandardCharsets.UTF_8);

        Supplier<BucketConfiguration> configurationSupplier = () -> BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(capacity)
                        .refillGreedy(capacity, Duration.ofSeconds(durationInSeconds)))
                .build();

        BucketProxy bucket = proxyManager.builder().build(bucketKey, configurationSupplier);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        } else {
            log.warn("Rate limit exceeded for IP: {} on URI: {}", clientIp, requestUri);

            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.setHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
            return false;
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
