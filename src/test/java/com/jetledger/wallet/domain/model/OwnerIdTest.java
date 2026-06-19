package com.jetledger.wallet.domain.model;

import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OwnerIdTest {

    @Test
    void shouldCreateFromUuid() {
        UUID uuid = UUID.randomUUID();
        OwnerId ownerId = OwnerId.from(uuid);
        assertEquals(uuid, ownerId.value());
    }

    @Test
    void shouldRejectNullUuid() {
        assertThrows(NullPointerException.class, () -> OwnerId.from(null));
    }

    @Test
    void shouldGenerateRandomId() {
        OwnerId ownerId = OwnerId.generate();
        assertNotNull(ownerId.value());
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        UUID uuid = UUID.randomUUID();
        OwnerId a = OwnerId.from(uuid);
        OwnerId b = OwnerId.from(uuid);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
