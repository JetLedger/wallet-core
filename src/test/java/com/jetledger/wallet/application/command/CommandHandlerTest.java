package com.jetledger.wallet.application.command;

import com.jetledger.wallet.domain.model.*;
import com.jetledger.wallet.domain.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CommandHandlerTest {

    private InMemoryWalletRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryWalletRepository();
    }

    @Test
    void shouldHandleCreateWalletCommand() {
        OwnerId ownerId = OwnerId.generate();
        CreateWalletCommand command = new CreateWalletCommand(ownerId, Currency.getInstance("USD"), UUID.randomUUID());
        CreateWalletHandler handler = new CreateWalletHandler(repository);
        WalletId walletId = handler.handle(command);
        assertNotNull(walletId);
        Optional<Wallet> saved = repository.findById(walletId);
        assertTrue(saved.isPresent());
        assertEquals(ownerId, saved.get().ownerId());
        assertEquals(BigDecimal.ZERO.setScale(2), saved.get().balance().amount());
    }

    @Test
    void shouldHandleDepositCommand() {
        Wallet wallet = Wallet.create(WalletId.generate(), OwnerId.generate(), Currency.getInstance("USD"));
        wallet.deposit(Money.of(new BigDecimal("100.00"), Currency.getInstance("USD")), UUID.randomUUID());
        repository.save(wallet);
        wallet.clearDomainEvents();
        DepositCommand command = new DepositCommand(wallet.id(),
            Money.of(new BigDecimal("50.00"), Currency.getInstance("USD")),
            UUID.randomUUID());
        DepositHandler handler = new DepositHandler(repository);
        handler.handle(command);
        Optional<Wallet> saved = repository.findById(wallet.id());
        assertTrue(saved.isPresent());
        assertEquals(new BigDecimal("150.00"), saved.get().balance().amount());
    }

    @Test
    void shouldHandleWithdrawCommandSuccessfully() {
        Wallet wallet = Wallet.create(WalletId.generate(), OwnerId.generate(), Currency.getInstance("USD"));
        wallet.deposit(Money.of(new BigDecimal("200.00"), Currency.getInstance("USD")), UUID.randomUUID());
        repository.save(wallet);
        wallet.clearDomainEvents();
        WithdrawCommand command = new WithdrawCommand(wallet.id(),
            Money.of(new BigDecimal("50.00"), Currency.getInstance("USD")),
            UUID.randomUUID());
        WithdrawHandler handler = new WithdrawHandler(repository);
        handler.handle(command);
        Optional<Wallet> saved = repository.findById(wallet.id());
        assertTrue(saved.isPresent());
        assertEquals(new BigDecimal("150.00"), saved.get().balance().amount());
    }

    @Test
    void shouldRejectWithdrawWhenInsufficientFunds() {
        Wallet wallet = Wallet.create(WalletId.generate(), OwnerId.generate(), Currency.getInstance("USD"));
        wallet.deposit(Money.of(new BigDecimal("30.00"), Currency.getInstance("USD")), UUID.randomUUID());
        repository.save(wallet);
        wallet.clearDomainEvents();
        WithdrawCommand command = new WithdrawCommand(wallet.id(),
            Money.of(new BigDecimal("100.00"), Currency.getInstance("USD")),
            UUID.randomUUID());
        WithdrawHandler handler = new WithdrawHandler(repository);
        assertThrows(WithdrawRejectedException.class, () -> handler.handle(command));
    }

    @Test
    void shouldRejectDepositToNonExistentWallet() {
        WalletId nonExistent = WalletId.generate();
        DepositCommand command = new DepositCommand(nonExistent,
            Money.of(new BigDecimal("50.00"), Currency.getInstance("USD")),
            UUID.randomUUID());
        DepositHandler handler = new DepositHandler(repository);
        assertThrows(NoSuchElementException.class, () -> handler.handle(command));
    }

    @Test
    void shouldRejectWithdrawFromNonExistentWallet() {
        WalletId nonExistent = WalletId.generate();
        WithdrawCommand command = new WithdrawCommand(nonExistent,
            Money.of(new BigDecimal("50.00"), Currency.getInstance("USD")),
            UUID.randomUUID());
        WithdrawHandler handler = new WithdrawHandler(repository);
        assertThrows(NoSuchElementException.class, () -> handler.handle(command));
    }

    static class InMemoryWalletRepository implements WalletRepository {
        private final Map<WalletId, Wallet> store = new HashMap<>();

        @Override
        public void save(Wallet wallet) {
            store.put(wallet.id(), wallet);
        }

        @Override
        public Optional<Wallet> findById(WalletId walletId) {
            Wallet stored = store.get(walletId);
            if (stored == null) return Optional.empty();
            return Optional.of(Wallet.reconstitute(
                stored.id(), stored.ownerId(), stored.currency(),
                stored.balance(), stored.version()));
        }
    }
}
