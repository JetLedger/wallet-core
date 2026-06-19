package com.jetledger.wallet.domain.event;

import com.jetledger.wallet.domain.model.Money;
import com.jetledger.wallet.domain.model.WalletId;
import java.time.Instant;
import java.util.UUID;

public record WithdrawRejected(WalletId walletId, Money amount, Money balance, String reason, UUID correlationId, Instant timestamp)
    implements WalletDomainEvent {

    public WithdrawRejected {
        if (timestamp == null) timestamp = Instant.now();
        if (correlationId == null) correlationId = UUID.randomUUID();
    }
}
