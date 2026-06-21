package com.jetledger.wallet.infrastructure.kafka;

import com.jetledger.wallet.domain.event.WalletDomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(value = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class DomainEventBridge {

    private final RedpandaEventPublisher redpandaPublisher;

    public DomainEventBridge(RedpandaEventPublisher redpandaPublisher) {
        this.redpandaPublisher = redpandaPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDomainEvent(WalletDomainEvent event) {
        log.debug("Bridging domain event to Redpanda: {}", event.getClass().getSimpleName());
        redpandaPublisher.publish(event)
            .exceptionally(ex -> {
                log.error("Failed to bridge domain event {} to Redpanda: {}",
                    event.getClass().getSimpleName(), ex.getMessage(), ex);
                return null;
            });
    }
}
