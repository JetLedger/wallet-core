package com.jetledger.wallet.infrastructure.persistence;

import com.jetledger.wallet.application.query.WalletDto;
import com.jetledger.wallet.application.query.WalletProjection;
import com.jetledger.wallet.domain.model.OwnerId;
import com.jetledger.wallet.domain.model.WalletId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Slf4j
@Repository
public class WalletProjectionImpl implements WalletProjection {

    private final SpringDataWalletRepository repository;

    public WalletProjectionImpl(SpringDataWalletRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<WalletDto> findById(WalletId walletId) {
        return repository.findById(walletId.value())
            .map(entity -> new WalletDto(
                WalletId.from(entity.getId()),
                OwnerId.from(entity.getOwnerId()),
                entity.getBalance(),
                entity.getCurrency(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()));
    }
}
