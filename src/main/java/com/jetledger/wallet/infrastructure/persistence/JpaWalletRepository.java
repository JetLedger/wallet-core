package com.jetledger.wallet.infrastructure.persistence;

import com.jetledger.wallet.domain.model.*;
import com.jetledger.wallet.domain.repository.WalletRepository;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Repository
public class JpaWalletRepository implements WalletRepository {

    private final SpringDataWalletRepository springDataRepository;

    public JpaWalletRepository(SpringDataWalletRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public void save(Wallet wallet) {
        WalletEntity entity = springDataRepository.findById(wallet.id().value())
            .map(existing -> {
                existing.setBalance(wallet.balance().amount());
                existing.setVersion(wallet.version());
                existing.setUpdatedAt(Instant.now());
                return existing;
            })
            .orElseGet(() -> new WalletEntity(
                wallet.id().value(),
                wallet.ownerId().value(),
                wallet.balance().amount(),
                wallet.currency(),
                wallet.version(),
                Instant.now(),
                Instant.now()));
        springDataRepository.save(entity);
    }

    @Override
    public Optional<Wallet> findById(WalletId walletId) {
        return springDataRepository.findById(walletId.value())
            .map(entity -> Wallet.reconstitute(
                WalletId.from(entity.getId()),
                OwnerId.from(entity.getOwnerId()),
                Currency.getInstance(entity.getCurrency()),
                Money.of(entity.getBalance(), Currency.getInstance(entity.getCurrency())),
                entity.getVersion()));
    }
}
