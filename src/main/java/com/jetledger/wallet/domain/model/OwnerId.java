package com.jetledger.wallet.domain.model;

import java.util.Objects;
import java.util.UUID;

public record OwnerId(UUID value) {

    public OwnerId {
        Objects.requireNonNull(value, "OwnerId value must not be null");
    }

    public static OwnerId from(UUID uuid) {
        return new OwnerId(uuid);
    }

    public static OwnerId generate() {
        return new OwnerId(UUID.randomUUID());
    }
}
