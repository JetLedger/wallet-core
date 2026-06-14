package com.jetledger.wallet.domain.event;

import com.jetledger.wallet.domain.model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DomainEventsTest {

    @Test
    void shouldCreateWalletCreatedEvent() {
        WalletId walletId = WalletId.generate();
        OwnerId ownerId = OwnerId.generate();
        Currency currency = Currency.getInstance("USD");
        Instant now = Instant.now();
        WalletCreated event = new WalletCreated(walletId, ownerId, currency, now);
        assertEquals(walletId, event.walletId());
        assertEquals(ownerId, event.ownerId());
        assertEquals(currency, event.currency());
        assertEquals(now, event.timestamp());
    }

    @Test
    void shouldCreateMoneyDepositedEvent() {
        WalletId walletId = WalletId.generate();
        Money amount = Money.of(new BigDecimal("100.00"), Currency.getInstance("USD"));
        Money balanceAfter = Money.of(new BigDecimal("100.00"), Currency.getInstance("USD"));
        UUID correlationId = UUID.randomUUID();
        Instant now = Instant.now();
        MoneyDeposited event = new MoneyDeposited(walletId, amount, balanceAfter, correlationId, now);
        assertEquals(walletId, event.walletId());
        assertEquals(amount, event.amount());
        assertEquals(balanceAfter, event.balanceAfter());
        assertEquals(correlationId, event.correlationId());
        assertEquals(now, event.timestamp());
    }

    @Test
    void shouldCreateMoneyWithdrawnEvent() {
        WalletId walletId = WalletId.generate();
        Money amount = Money.of(new BigDecimal("50.00"), Currency.getInstance("USD"));
        Money balanceAfter = Money.of(new BigDecimal("50.00"), Currency.getInstance("USD"));
        UUID correlationId = UUID.randomUUID();
        Instant now = Instant.now();
        MoneyWithdrawn event = new MoneyWithdrawn(walletId, amount, balanceAfter, correlationId, now);
        assertEquals(walletId, event.walletId());
        assertEquals(amount, event.amount());
        assertEquals(balanceAfter, event.balanceAfter());
        assertEquals(correlationId, event.correlationId());
        assertEquals(now, event.timestamp());
    }

    @Test
    void shouldCreateWithdrawRejectedEvent() {
        WalletId walletId = WalletId.generate();
        Money amount = Money.of(new BigDecimal("100.00"), Currency.getInstance("USD"));
        Money balance = Money.of(new BigDecimal("30.00"), Currency.getInstance("USD"));
        String reason = "Insufficient funds";
        UUID correlationId = UUID.randomUUID();
        Instant now = Instant.now();
        WithdrawRejected event = new WithdrawRejected(walletId, amount, balance, reason, correlationId, now);
        assertEquals(walletId, event.walletId());
        assertEquals(amount, event.amount());
        assertEquals(balance, event.balance());
        assertEquals(reason, event.reason());
        assertEquals(correlationId, event.correlationId());
        assertEquals(now, event.timestamp());
    }

    @Test
    void allEventsShouldImplementDomainEventInterface() {
        WalletId walletId = WalletId.generate();
        OwnerId ownerId = OwnerId.generate();
        Money amount = Money.of(new BigDecimal("50.00"), Currency.getInstance("USD"));
        Money balance = Money.of(new BigDecimal("100.00"), Currency.getInstance("USD"));
        UUID correlationId = UUID.randomUUID();
        Instant now = Instant.now();

        assertInstanceOf(WalletDomainEvent.class, new WalletCreated(walletId, ownerId, Currency.getInstance("USD"), now));
        assertInstanceOf(WalletDomainEvent.class, new MoneyDeposited(walletId, amount, balance, correlationId, now));
        assertInstanceOf(WalletDomainEvent.class, new MoneyWithdrawn(walletId, amount, balance, correlationId, now));
        assertInstanceOf(WalletDomainEvent.class, new WithdrawRejected(walletId, amount, balance, "reason", correlationId, now));
    }
}
