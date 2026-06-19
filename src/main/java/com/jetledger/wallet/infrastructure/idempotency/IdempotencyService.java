package com.jetledger.wallet.infrastructure.idempotency;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyService {

    Optional<IdempotencyRecord> get(UUID idempotencyKey);

    void store(UUID idempotencyKey, IdempotencyRecord record);
}
