package com.jetledger.wallet.application.command;

import com.jetledger.wallet.domain.model.Money;
import com.jetledger.wallet.domain.model.WalletId;
import java.util.Objects;
import java.util.UUID;

public record DepositCommand(WalletId walletId, Money amount, UUID correlationId) {

    public DepositCommand {
        Objects.requireNonNull(walletId, "walletId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        if (correlationId == null) correlationId = UUID.randomUUID();
    }
}
