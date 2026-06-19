package com.jetledger.wallet.domain.model;

import com.jetledger.wallet.domain.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    private WalletId walletId;
    private OwnerId ownerId;
    private Currency usd;
    private Money initialBalance;

    @BeforeEach
    void setUp() {
        walletId = WalletId.generate();
        ownerId = OwnerId.generate();
        usd = Currency.getInstance("USD");
        initialBalance = Money.of(BigDecimal.ZERO, usd);
    }

    @Test
    void shouldCreateWalletWithZeroBalance() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        assertEquals(walletId, wallet.id());
        assertEquals(ownerId, wallet.ownerId());
        assertEquals(usd, wallet.currency());
        assertEquals(BigDecimal.ZERO.setScale(2), wallet.balance().amount());
    }

    @Test
    void shouldEmitWalletCreatedEventOnCreation() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        List<WalletDomainEvent> events = wallet.domainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(WalletCreated.class, events.get(0));
        WalletCreated event = (WalletCreated) events.get(0);
        assertEquals(walletId, event.walletId());
        assertEquals(ownerId, event.ownerId());
    }

    @Test
    void shouldDepositMoney() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        Money amount = Money.of(new BigDecimal("100.00"), usd);
        wallet.deposit(amount, UUID.randomUUID());
        assertEquals(new BigDecimal("100.00"), wallet.balance().amount());
    }

    @Test
    void shouldEmitMoneyDepositedEvent() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        wallet.clearDomainEvents();
        Money amount = Money.of(new BigDecimal("100.00"), usd);
        wallet.deposit(amount, UUID.randomUUID());
        List<WalletDomainEvent> events = wallet.domainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(MoneyDeposited.class, events.get(0));
        MoneyDeposited event = (MoneyDeposited) events.get(0);
        assertEquals(amount, event.amount());
        assertEquals(new BigDecimal("100.00"), event.balanceAfter().amount());
    }

    @Test
    void shouldRejectDepositWithDifferentCurrency() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        Money amount = Money.of(new BigDecimal("100.00"), Currency.getInstance("EUR"));
        assertThrows(IllegalArgumentException.class, () -> wallet.deposit(amount, UUID.randomUUID()));
    }

    @Test
    void shouldRejectDepositOfZero() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        Money zero = Money.of(BigDecimal.ZERO, usd);
        assertThrows(IllegalArgumentException.class, () -> wallet.deposit(zero, UUID.randomUUID()));
    }

    @Test
    void shouldRejectDepositOfNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () ->
            Money.of(new BigDecimal("-50.00"), usd));
    }

    @Test
    void shouldWithdrawMoney() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        wallet.deposit(Money.of(new BigDecimal("200.00"), usd), UUID.randomUUID());
        wallet.clearDomainEvents();
        Money amount = Money.of(new BigDecimal("50.00"), usd);
        wallet.withdraw(amount, UUID.randomUUID());
        assertEquals(new BigDecimal("150.00"), wallet.balance().amount());
    }

    @Test
    void shouldEmitMoneyWithdrawnEventOnSuccessfulWithdraw() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        wallet.deposit(Money.of(new BigDecimal("200.00"), usd), UUID.randomUUID());
        wallet.clearDomainEvents();
        Money amount = Money.of(new BigDecimal("50.00"), usd);
        wallet.withdraw(amount, UUID.randomUUID());
        List<WalletDomainEvent> events = wallet.domainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(MoneyWithdrawn.class, events.get(0));
        MoneyWithdrawn event = (MoneyWithdrawn) events.get(0);
        assertEquals(amount, event.amount());
        assertEquals(new BigDecimal("150.00"), event.balanceAfter().amount());
    }

    @Test
    void shouldRejectWithdrawWithInsufficientFunds() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        wallet.deposit(Money.of(new BigDecimal("30.00"), usd), UUID.randomUUID());
        wallet.clearDomainEvents();
        Money amount = Money.of(new BigDecimal("100.00"), usd);
        wallet.withdraw(amount, UUID.randomUUID());
        assertEquals(new BigDecimal("30.00"), wallet.balance().amount());
    }

    @Test
    void shouldEmitWithdrawRejectedEventOnInsufficientFunds() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        wallet.deposit(Money.of(new BigDecimal("30.00"), usd), UUID.randomUUID());
        wallet.clearDomainEvents();
        Money amount = Money.of(new BigDecimal("100.00"), usd);
        wallet.withdraw(amount, UUID.randomUUID());
        List<WalletDomainEvent> events = wallet.domainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(WithdrawRejected.class, events.get(0));
        WithdrawRejected event = (WithdrawRejected) events.get(0);
        assertEquals(amount, event.amount());
        assertEquals(new BigDecimal("30.00"), event.balance().amount());
        assertTrue(event.reason().toLowerCase().contains("insufficient"));
    }

    @Test
    void shouldRejectWithdrawWithDifferentCurrency() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        wallet.deposit(Money.of(new BigDecimal("100.00"), usd), UUID.randomUUID());
        wallet.clearDomainEvents();
        Money amount = Money.of(new BigDecimal("50.00"), Currency.getInstance("EUR"));
        assertThrows(IllegalArgumentException.class, () -> wallet.withdraw(amount, UUID.randomUUID()));
    }

    @Test
    void shouldRejectWithdrawOfZero() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        wallet.deposit(Money.of(new BigDecimal("100.00"), usd), UUID.randomUUID());
        Money zero = Money.of(BigDecimal.ZERO, usd);
        assertThrows(IllegalArgumentException.class, () -> wallet.withdraw(zero, UUID.randomUUID()));
    }

    @Test
    void shouldRejectWithdrawOfNegativeAmount() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        wallet.deposit(Money.of(new BigDecimal("100.00"), usd), UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () ->
            Money.of(new BigDecimal("-50.00"), usd));
    }

    @Test
    void shouldNotAllowBalanceToGoNegative() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        wallet.deposit(Money.of(new BigDecimal("10.00"), usd), UUID.randomUUID());
        wallet.withdraw(Money.of(new BigDecimal("5.00"), usd), UUID.randomUUID());
        assertEquals(new BigDecimal("5.00"), wallet.balance().amount());
    }

    @Test
    void shouldIncrementVersionOnDeposit() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        long initialVersion = wallet.version();
        wallet.deposit(Money.of(new BigDecimal("50.00"), usd), UUID.randomUUID());
        assertEquals(initialVersion + 1, wallet.version());
    }

    @Test
    void shouldIncrementVersionOnWithdraw() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        wallet.deposit(Money.of(new BigDecimal("100.00"), usd), UUID.randomUUID());
        long versionBefore = wallet.version();
        wallet.withdraw(Money.of(new BigDecimal("30.00"), usd), UUID.randomUUID());
        assertEquals(versionBefore + 1, wallet.version());
    }

    @Test
    void shouldClearDomainEvents() {
        Wallet wallet = Wallet.create(walletId, ownerId, usd);
        wallet.clearDomainEvents();
        assertTrue(wallet.domainEvents().isEmpty());
    }
}
