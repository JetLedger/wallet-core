package com.jetledger.wallet.infrastructure.idempotency;

import java.time.Instant;

public record IdempotencyRecord(
    String idempotencyKey,
    int responseStatus,
    String responseBody,
    String requestHash,
    Instant createdAt
) {}
