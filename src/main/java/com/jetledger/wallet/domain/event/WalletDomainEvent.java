package com.jetledger.wallet.domain.event;

import java.time.Instant;

public interface WalletDomainEvent {

    Instant timestamp();
}
