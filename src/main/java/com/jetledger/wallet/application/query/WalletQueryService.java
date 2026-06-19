package com.jetledger.wallet.application.query;

import com.jetledger.wallet.domain.model.WalletId;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class WalletQueryService {

    private final WalletProjection projection;

    public WalletQueryService(WalletProjection projection) {
        this.projection = projection;
    }

    public Optional<WalletDto> findById(WalletId walletId) {
        return projection.findById(walletId);
    }
}
