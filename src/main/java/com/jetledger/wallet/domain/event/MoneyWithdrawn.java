package com.jetledger.wallet.domain.event;

import com.jetledger.wallet.domain.model.Money;
import com.jetledger.wallet.domain.model.WalletId;
import java.time.Instant;
import java.util.UUID;

public record MoneyWithdrawn(WalletId walletId, Money amount, Money balanceAfter, UUID correlationId, Instant timestamp)
    implements WalletDomainEvent {

    public MoneyWithdrawn {
        if (timestamp == null) timestamp = Instant.now();
    }
}
