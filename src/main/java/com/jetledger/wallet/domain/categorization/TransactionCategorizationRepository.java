package com.jetledger.wallet.domain.categorization;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TransactionCategorizationRepository extends JpaRepository<TransactionCategorization, UUID> {
    Optional<TransactionCategorization> findByEventId(String eventId);
    boolean existsByEventId(String eventId);
}
