package com.jetledger.wallet.infrastructure.event;

import com.jetledger.wallet.domain.event.WalletDomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringDomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SpringDomainEventPublisher.class);

    private final ApplicationEventPublisher springPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    public void publish(WalletDomainEvent event) {
        log.info("Domain event: {} | correlationId: {} | timestamp: {}",
            event.getClass().getSimpleName(), extractCorrelationId(event), event.timestamp());
        springPublisher.publishEvent(event);
    }

    private String extractCorrelationId(WalletDomainEvent event) {
        return switch (event) {
            case com.jetledger.wallet.domain.event.MoneyDeposited e -> e.correlationId().toString();
            case com.jetledger.wallet.domain.event.MoneyWithdrawn e -> e.correlationId().toString();
            case com.jetledger.wallet.domain.event.WithdrawRejected e -> e.correlationId().toString();
            default -> "N/A";
        };
    }
}
