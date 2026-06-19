package com.jetledger.wallet.infrastructure.idempotency;

import tools.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class IdempotencyConfiguration {

    @Value("${idempotency.ttl:PT24H}")
    private Duration ttl;

    @Bean
    @ConditionalOnProperty(name = "idempotency.redis.enabled", havingValue = "true")
    public IdempotencyService redisIdempotencyService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        return new RedisIdempotencyService(redisTemplate, objectMapper, ttl);
    }

    @Bean
    @ConditionalOnProperty(name = "idempotency.redis.enabled", havingValue = "false", matchIfMissing = true)
    public IdempotencyService inMemoryIdempotencyService() {
        return new InMemoryIdempotencyService(ttl);
    }
}
