package com.jetledger.wallet.infrastructure.idempotency;

import java.util.Optional;

public interface IdempotencyService {

    Optional<CachedResponse> claim(IdempotencyKey key, String requestHash);

    void storeResult(IdempotencyKey key, CachedResponse response);
}
