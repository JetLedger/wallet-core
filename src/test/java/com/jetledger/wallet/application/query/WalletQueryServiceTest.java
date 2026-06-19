package com.jetledger.wallet.application.query;

import com.jetledger.wallet.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WalletQueryServiceTest {

    private InMemoryWalletProjection projection;
    private WalletQueryService queryService;

    @BeforeEach
    void setUp() {
        projection = new InMemoryWalletProjection();
        queryService = new WalletQueryService(projection);
    }

    @Test
    void shouldReturnWalletDtoWhenWalletExists() {
        WalletId walletId = WalletId.generate();
        OwnerId ownerId = OwnerId.generate();
        WalletDto dto = new WalletDto(walletId, ownerId, new BigDecimal("100.00"), "USD", Instant.now(), Instant.now());
        projection.save(dto);
        Optional<WalletDto> result = queryService.findById(walletId);
        assertTrue(result.isPresent());
        assertEquals(walletId, result.get().id());
        assertEquals(ownerId, result.get().ownerId());
        assertEquals(new BigDecimal("100.00"), result.get().balance());
        assertEquals("USD", result.get().currency());
    }

    @Test
    void shouldReturnEmptyWhenWalletDoesNotExist() {
        Optional<WalletDto> result = queryService.findById(WalletId.generate());
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotReturnAggregateDirectly() {
        assertFalse(Arrays.stream(WalletQueryService.class.getMethods())
            .anyMatch(m -> m.getReturnType().equals(Wallet.class)),
            "Query service must not expose the Wallet aggregate");
    }

    static class InMemoryWalletProjection implements WalletProjection {
        private final Map<WalletId, WalletDto> store = new HashMap<>();

        @Override
        public Optional<WalletDto> findById(WalletId walletId) {
            return Optional.ofNullable(store.get(walletId));
        }

        void save(WalletDto dto) {
            store.put(dto.id(), dto);
        }
    }
}
