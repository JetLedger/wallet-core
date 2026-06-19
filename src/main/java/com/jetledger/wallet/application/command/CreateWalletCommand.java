package com.jetledger.wallet.application.command;

import com.jetledger.wallet.domain.model.OwnerId;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

public record CreateWalletCommand(OwnerId ownerId, Currency currency, UUID correlationId) {

    public CreateWalletCommand {
        Objects.requireNonNull(ownerId, "ownerId must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (correlationId == null) correlationId = UUID.randomUUID();
    }
}
