package com.jetledger.wallet.infrastructure.idempotency;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisIdempotencyService implements IdempotencyService {

    private static final String KEY_PREFIX = "idempotency:";
    private static final Logger log = LoggerFactory.getLogger(RedisIdempotencyService.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public RedisIdempotencyService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = ttl;
    }

    private String key(UUID idempotencyKey) {
        return KEY_PREFIX + idempotencyKey;
    }

    @Override
    public Optional<IdempotencyRecord> get(UUID idempotencyKey) {
        String json = redisTemplate.opsForValue().get(key(idempotencyKey));
        if (json == null) return Optional.empty();
        try {
            return Optional.of(objectMapper.readValue(json, IdempotencyRecord.class));
        } catch (JacksonException e) {
            log.warn("Failed to deserialize idempotency record for key {}", idempotencyKey, e);
            return Optional.empty();
        }
    }

    @Override
    public void store(UUID idempotencyKey, IdempotencyRecord record) {
        try {
            String json = objectMapper.writeValueAsString(record);
            redisTemplate.opsForValue().set(key(idempotencyKey), json, ttl);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize idempotency record", e);
        }
    }
}
