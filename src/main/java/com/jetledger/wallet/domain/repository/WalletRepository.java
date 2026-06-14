package com.jetledger.wallet.domain.repository;

import com.jetledger.wallet.domain.model.Wallet;
import com.jetledger.wallet.domain.model.WalletId;
import java.util.Optional;

public interface WalletRepository {

    void save(Wallet wallet);

    Optional<Wallet> findById(WalletId walletId);
}
