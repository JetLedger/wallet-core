package com.jetledger.wallet.interfaces.api;

import com.jetledger.wallet.application.query.WalletDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletResponse(
    UUID walletId,
    BigDecimal balance,
    String currency,
    Instant createdAt,
    Instant updatedAt
) {
    public static WalletResponse from(WalletDto dto) {
        return new WalletResponse(
            dto.id().value(),
            dto.balance(),
            dto.currency(),
            dto.createdAt(),
            dto.updatedAt());
    }
}
