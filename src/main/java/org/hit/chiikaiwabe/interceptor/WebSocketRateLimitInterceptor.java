package org.hit.chiikaiwabe.interceptor;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketRateLimitInterceptor implements ChannelInterceptor {

    private static final long WS_CAPACITY = 30;
    private static final long WS_DURATION_SECONDS = 60;

    private final ProxyManager<byte[]> proxyManager;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.SEND.equals(accessor.getCommand())) {
            return message;
        }

        Principal user = accessor.getUser();
        if (user == null) {
            return message;
        }

        String userId = user.getName();
        String destination = accessor.getDestination();

        String bucketKeyString = "WS_RATE_LIMIT:" + userId;
        byte[] bucketKey = bucketKeyString.getBytes(StandardCharsets.UTF_8);

        Supplier<BucketConfiguration> configurationSupplier = () -> BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(WS_CAPACITY)
                        .refillGreedy(WS_CAPACITY, Duration.ofSeconds(WS_DURATION_SECONDS)))
                .build();

        BucketProxy bucket = proxyManager.builder().build(bucketKey, configurationSupplier);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            return message;
        } else {
            log.warn("WebSocket rate limit exceeded for user: {} on destination: {}", userId, destination);
            return null;
        }
    }
}
