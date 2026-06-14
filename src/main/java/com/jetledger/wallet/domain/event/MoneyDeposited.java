package com.jetledger.wallet.domain.event;

import com.jetledger.wallet.domain.model.Money;
import com.jetledger.wallet.domain.model.WalletId;
import java.time.Instant;
import java.util.UUID;

public record MoneyDeposited(WalletId walletId, Money amount, Money balanceAfter, UUID correlationId, Instant timestamp)
    implements WalletDomainEvent {

    public MoneyDeposited {
        if (timestamp == null) timestamp = Instant.now();
    }
}
