package org.hit.chiikaiwabe.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RateLimitConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setJmxEnabled(false);
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            return new JedisPool(poolConfig, redisHost, redisPort, 2000, redisPassword);
        }
        return new JedisPool(poolConfig, redisHost, redisPort);
    }

    @Bean
    public ProxyManager<byte[]> proxyManager(JedisPool jedisPool) {
        return JedisBasedProxyManager.builderFor(jedisPool).build();
    }
}
