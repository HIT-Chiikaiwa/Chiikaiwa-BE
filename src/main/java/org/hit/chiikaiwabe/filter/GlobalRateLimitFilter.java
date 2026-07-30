package org.hit.chiikaiwabe.filter;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.hit.chiikaiwabe.security.IpAbuseTracker;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
public class GlobalRateLimitFilter extends OncePerRequestFilter {

    private static final long GLOBAL_CAPACITY = 100;
    private static final long GLOBAL_REFILL_SECONDS = 1;

    private final Bucket globalBucket;
    private final IpAbuseTracker ipAbuseTracker;

    public GlobalRateLimitFilter(IpAbuseTracker ipAbuseTracker) {
        this.ipAbuseTracker = ipAbuseTracker;
        this.globalBucket = Bucket.builder()
                .addLimit(limit -> limit.capacity(GLOBAL_CAPACITY)
                        .refillGreedy(GLOBAL_CAPACITY, Duration.ofSeconds(GLOBAL_REFILL_SECONDS)))
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIP(request);

        if (ipAbuseTracker.isBanned(clientIp)) {
            log.warn("Request blocked from banned IP: {}", clientIp);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\": \"Your IP has been temporarily banned due to excessive requests.\"}");
            return;
        }

        if (!globalBucket.tryConsume(1)) {
            log.error("Global rate limit exceeded! Server under heavy load. Request from IP: {}", clientIp);
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\": \"Server is under heavy load. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
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
