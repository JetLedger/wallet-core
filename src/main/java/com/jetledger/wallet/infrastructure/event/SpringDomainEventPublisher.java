package com.jetledger.wallet.infrastructure.event;

import com.jetledger.wallet.domain.event.MoneyDeposited;
import com.jetledger.wallet.domain.event.MoneyWithdrawn;
import com.jetledger.wallet.domain.event.WalletDomainEvent;
import com.jetledger.wallet.domain.event.WithdrawRejected;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SpringDomainEventPublisher {

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
            case MoneyDeposited e -> e.correlationId().toString();
            case MoneyWithdrawn e -> e.correlationId().toString();
            case WithdrawRejected e -> e.correlationId().toString();
            default -> "N/A";
        };
    }
}
