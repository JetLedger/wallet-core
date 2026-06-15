package com.jetledger.wallet.application.command;

import com.jetledger.wallet.domain.model.Wallet;
import com.jetledger.wallet.domain.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DepositHandler {

    private final WalletRepository repository;

    public DepositHandler(WalletRepository repository) {
        this.repository = repository;
    }

    public void handle(DepositCommand command) {
        Wallet wallet = repository.findById(command.walletId())
            .orElseThrow();
        wallet.deposit(command.amount());
        repository.save(wallet);
    }
}
