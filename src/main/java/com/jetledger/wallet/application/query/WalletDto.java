package com.jetledger.wallet.application.query;

import com.jetledger.wallet.domain.model.OwnerId;
import com.jetledger.wallet.domain.model.WalletId;
import java.math.BigDecimal;
import java.time.Instant;

public record WalletDto(WalletId id, OwnerId ownerId, BigDecimal balance, String currency, Instant createdAt, Instant updatedAt) {
}
