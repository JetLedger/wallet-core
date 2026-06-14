package com.jetledger.wallet.domain.event;

import com.jetledger.wallet.domain.model.OwnerId;
import com.jetledger.wallet.domain.model.WalletId;
import java.time.Instant;
import java.util.Currency;

public record WalletCreated(WalletId walletId, OwnerId ownerId, Currency currency, Instant timestamp)
    implements WalletDomainEvent {

    public WalletCreated {
        if (timestamp == null) timestamp = Instant.now();
    }
}
