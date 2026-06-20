package com.jetledger.wallet.infrastructure.idempotency;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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

    private String key(IdempotencyKey idempotencyKey) {
        return KEY_PREFIX + idempotencyKey.toCacheKey();
    }

    @Override
    public Optional<CachedResponse> claim(IdempotencyKey idempotencyKey, String requestHash) {
        String cacheKey = key(idempotencyKey);
        String placeholderJson;
        try {
            IdempotencyRecord placeholder = new IdempotencyRecord(
                idempotencyKey.toCacheKey(), 0, "", requestHash, Instant.now());
            placeholderJson = objectMapper.writeValueAsString(placeholder);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize idempotency placeholder", e);
        }

        Boolean claimed = redisTemplate.opsForValue().setIfAbsent(cacheKey, placeholderJson, ttl);
        if (Boolean.TRUE.equals(claimed)) {
            return Optional.empty();
        }

        String json = redisTemplate.opsForValue().get(cacheKey);
        if (json == null) {
            return Optional.empty();
        }

        IdempotencyRecord existing;
        try {
            existing = objectMapper.readValue(json, IdempotencyRecord.class);
        } catch (JacksonException e) {
            log.error("Failed to deserialize idempotency record for key={}", cacheKey, e);
            throw new IdempotencyStoreCorruptedException(
                "Idempotency record for key " + cacheKey + " is corrupted", e);
        }

        if (!existing.requestHash().equals(requestHash)) {
            return Optional.of(new CachedResponse(422,
                "{\"error\":\"IDEMPOTENCY_CONFLICT\",\"message\":\"Idempotency key already used with different request body\"}"));
        }

        return Optional.of(new CachedResponse(existing.responseStatus(), existing.responseBody()));
    }

    @Override
    public void storeResult(IdempotencyKey idempotencyKey, CachedResponse response) {
        try {
            IdempotencyRecord record = new IdempotencyRecord(
                idempotencyKey.toCacheKey(), response.responseStatus(), response.responseBody(),
                "", Instant.now());
            String json = objectMapper.writeValueAsString(record);
            redisTemplate.opsForValue().set(key(idempotencyKey), json, ttl);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize idempotency record", e);
        }
    }
}
