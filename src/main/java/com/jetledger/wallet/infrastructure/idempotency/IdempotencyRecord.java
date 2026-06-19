package com.jetledger.wallet.infrastructure.idempotency;

import java.time.Instant;
import java.util.UUID;

public record IdempotencyRecord(
    UUID idempotencyKey,
    int responseStatus,
    String responseBody,
    String requestHash,
    Instant createdAt
) {}
