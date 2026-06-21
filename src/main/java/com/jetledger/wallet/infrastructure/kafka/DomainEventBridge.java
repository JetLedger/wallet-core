package com.jetledger.wallet.infrastructure.kafka;

import com.jetledger.wallet.domain.event.WalletDomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(value = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class DomainEventBridge {

    private final RedpandaEventPublisher redpandaPublisher;

    public DomainEventBridge(RedpandaEventPublisher redpandaPublisher) {
        this.redpandaPublisher = redpandaPublisher;
    }

    @EventListener
    public void onDomainEvent(WalletDomainEvent event) {
        log.debug("Bridging domain event to Redpanda: {}", event.getClass().getSimpleName());
        redpandaPublisher.publish(event);
    }
}
