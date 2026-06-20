package com.jetledger.wallet.infrastructure.idempotency;

import com.jetledger.wallet.domain.model.WalletId;
import java.util.Objects;
import java.util.UUID;

public record IdempotencyKey(UUID clientKey, String route, WalletId walletId) {

    public IdempotencyKey {
        Objects.requireNonNull(clientKey, "clientKey must not be null");
        Objects.requireNonNull(route, "route must not be null");
        Objects.requireNonNull(walletId, "walletId must not be null");
    }

    public String toCacheKey() {
        return route + ":" + walletId.value() + ":" + clientKey;
    }
}
