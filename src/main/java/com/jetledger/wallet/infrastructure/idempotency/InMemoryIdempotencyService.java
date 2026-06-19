package com.jetledger.wallet.infrastructure.idempotency;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryIdempotencyService implements IdempotencyService {

    private final Map<UUID, IdempotencyRecord> store = new ConcurrentHashMap<>();
    private final Duration ttl;

    public InMemoryIdempotencyService(Duration ttl) {
        this.ttl = ttl;
    }

    @Override
    public Optional<IdempotencyRecord> get(UUID idempotencyKey) {
        IdempotencyRecord record = store.get(idempotencyKey);
        if (record == null) return Optional.empty();
        if (Duration.between(record.createdAt(), Instant.now()).compareTo(ttl) > 0) {
            store.remove(idempotencyKey);
            return Optional.empty();
        }
        return Optional.of(record);
    }

    @Override
    public void store(UUID idempotencyKey, IdempotencyRecord record) {
        store.put(idempotencyKey, record);
    }
}
