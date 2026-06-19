package com.jetledger.wallet.domain.model;

import java.util.Objects;
import java.util.UUID;

public record WalletId(UUID value) {

    public WalletId {
        Objects.requireNonNull(value, "WalletId value must not be null");
    }

    public static WalletId from(UUID uuid) {
        return new WalletId(uuid);
    }

    public static WalletId generate() {
        return new WalletId(UUID.randomUUID());
    }
}
