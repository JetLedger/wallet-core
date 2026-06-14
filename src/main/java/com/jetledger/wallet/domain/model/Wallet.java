package com.jetledger.wallet.domain.model;

import com.jetledger.wallet.domain.event.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class Wallet {

    private final WalletId id;
    private final OwnerId ownerId;
    private final Currency currency;
    private Money balance;
    private long version;
    private final List<WalletDomainEvent> domainEvents = new ArrayList<>();

    private Wallet(WalletId id, OwnerId ownerId, Currency currency) {
        this.id = Objects.requireNonNull(id);
        this.ownerId = Objects.requireNonNull(ownerId);
        this.currency = Objects.requireNonNull(currency);
        this.balance = Money.of(BigDecimal.ZERO, currency);
        this.version = 0;
    }

    public static Wallet create(WalletId id, OwnerId ownerId, Currency currency) {
        Wallet wallet = new Wallet(id, ownerId, currency);
        wallet.domainEvents.add(new WalletCreated(id, ownerId, currency, Instant.now()));
        return wallet;
    }

    public static Wallet reconstitute(WalletId id, OwnerId ownerId, Currency currency, Money balance, long version) {
        Wallet wallet = new Wallet(id, ownerId, currency);
        wallet.balance = balance;
        wallet.version = version;
        return wallet;
    }

    public void deposit(Money amount) {
        if (amount.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("deposit amount must be positive");
        }
        if (!amount.currency().equals(this.currency)) {
            throw new IllegalArgumentException("currency mismatch");
        }
        this.balance = this.balance.add(amount);
        this.version++;
        this.domainEvents.add(new MoneyDeposited(id, amount, balance, UUID.randomUUID(), Instant.now()));
    }

    public void withdraw(Money amount) {
        if (amount.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("withdraw amount must be positive");
        }
        if (!amount.currency().equals(this.currency)) {
            throw new IllegalArgumentException("currency mismatch");
        }
        if (amount.amount().compareTo(this.balance.amount()) > 0) {
            this.domainEvents.add(new WithdrawRejected(id, amount, balance, "Insufficient funds", UUID.randomUUID(), Instant.now()));
            return;
        }
        this.balance = this.balance.subtract(amount);
        this.version++;
        this.domainEvents.add(new MoneyWithdrawn(id, amount, balance, UUID.randomUUID(), Instant.now()));
    }

    public WalletId id() { return id; }
    public OwnerId ownerId() { return ownerId; }
    public Currency currency() { return currency; }
    public Money balance() { return balance; }
    public long version() { return version; }

    public List<WalletDomainEvent> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
