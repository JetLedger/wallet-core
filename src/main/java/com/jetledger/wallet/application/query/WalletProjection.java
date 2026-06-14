package com.jetledger.wallet.application.query;

import com.jetledger.wallet.domain.model.WalletId;
import java.util.Optional;

public interface WalletProjection {

    Optional<WalletDto> findById(WalletId walletId);
}
