package com.jetledger.wallet.application.command;

import com.jetledger.wallet.domain.event.WithdrawRejected;
import com.jetledger.wallet.domain.model.Wallet;
import com.jetledger.wallet.domain.repository.WalletRepository;

public class WithdrawHandler {

    private final WalletRepository repository;

    public WithdrawHandler(WalletRepository repository) {
        this.repository = repository;
    }

    public void handle(WithdrawCommand command) {
        Wallet wallet = repository.findById(command.walletId())
            .orElseThrow();
        wallet.withdraw(command.amount());
        boolean rejected = wallet.domainEvents().stream()
            .anyMatch(e -> e instanceof WithdrawRejected);
        if (rejected) {
            throw new WithdrawRejectedException("Insufficient funds", wallet.balance());
        }
        repository.save(wallet);
    }
}
