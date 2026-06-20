package com.jetledger.wallet.infrastructure.idempotency;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

public class InMemoryIdempotencyService implements IdempotencyService {

    private final Cache<String, IdempotencyRecord> store;
    private final Duration ttl;

    public InMemoryIdempotencyService(Duration ttl) {
        this.ttl = ttl;
        this.store = Caffeine.newBuilder()
            .expireAfterWrite(ttl)
            .build();
    }

    @Override
    public Optional<CachedResponse> claim(IdempotencyKey key, String requestHash) {
        String cacheKey = key.toCacheKey();
        ConcurrentMap<String, IdempotencyRecord> map = store.asMap();

        IdempotencyRecord placeholder = new IdempotencyRecord(
            cacheKey, 0, "", requestHash, Instant.now());

        IdempotencyRecord existing = map.putIfAbsent(cacheKey, placeholder);
        if (existing == null) {
            return Optional.empty();
        }

        if (existing.responseStatus() == 0 && existing.responseBody().isEmpty()) {
            long deadline = System.nanoTime() + 2_000_000_000L;
            while (System.nanoTime() < deadline) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                IdempotencyRecord updated = map.get(cacheKey);
                if (updated != null && updated.responseStatus() != 0) {
                    existing = updated;
                    break;
                }
            }
        }

        if (!existing.requestHash().equals(requestHash)) {
            return Optional.of(new CachedResponse(422,
                "{\"error\":\"IDEMPOTENCY_CONFLICT\",\"message\":\"Idempotency key already used with different request body\"}"));
        }

        return Optional.of(new CachedResponse(existing.responseStatus(), existing.responseBody()));
    }

    @Override
    public void storeResult(IdempotencyKey key, CachedResponse response) {
        String cacheKey = key.toCacheKey();
        store.asMap().computeIfPresent(cacheKey, (k, existing) ->
            new IdempotencyRecord(cacheKey, response.responseStatus(), response.responseBody(),
                existing.requestHash(), existing.createdAt()));
    }
}
