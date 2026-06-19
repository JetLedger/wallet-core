package com.jetledger.wallet.application.command;

import com.jetledger.wallet.domain.model.Money;

public class WithdrawRejectedException extends RuntimeException {

    private final Money balance;

    public WithdrawRejectedException(String message, Money balance) {
        super(message);
        this.balance = balance;
    }

    public Money balance() {
        return balance;
    }
}
