package com.jetledger.wallet.domain.model;

import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WalletIdTest {

    @Test
    void shouldCreateFromUuid() {
        UUID uuid = UUID.randomUUID();
        WalletId walletId = WalletId.from(uuid);
        assertEquals(uuid, walletId.value());
    }

    @Test
    void shouldRejectNullUuid() {
        assertThrows(NullPointerException.class, () -> WalletId.from(null));
    }

    @Test
    void shouldGenerateRandomId() {
        WalletId walletId = WalletId.generate();
        assertNotNull(walletId.value());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        UUID uuid = UUID.randomUUID();
        WalletId a = WalletId.from(uuid);
        WalletId b = WalletId.from(uuid);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void twoDifferentIdsShouldNotBeEqual() {
        WalletId a = WalletId.generate();
        WalletId b = WalletId.generate();
        assertNotEquals(a, b);
    }
}
