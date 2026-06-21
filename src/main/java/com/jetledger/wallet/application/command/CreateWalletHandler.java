package com.jetledger.wallet.application.command;

import com.jetledger.wallet.domain.model.Wallet;
import com.jetledger.wallet.domain.model.WalletId;
import com.jetledger.wallet.domain.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CreateWalletHandler {

    private final WalletRepository repository;

    public CreateWalletHandler(WalletRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public WalletId handle(CreateWalletCommand command) {
        WalletId walletId = WalletId.generate();
        Wallet wallet = Wallet.create(walletId, command.ownerId(), command.currency());
        repository.save(wallet);
        return walletId;
    }
}
