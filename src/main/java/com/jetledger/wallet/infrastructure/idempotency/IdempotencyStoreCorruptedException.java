package com.jetledger.wallet.infrastructure.idempotency;

public class IdempotencyStoreCorruptedException extends RuntimeException {

    public IdempotencyStoreCorruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
