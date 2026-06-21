package com.jetledger.wallet.infrastructure.kafka;

import com.jetledger.wallet.domain.event.WalletDomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(value = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class RedpandaEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public RedpandaEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<Void> publish(WalletDomainEvent event) {
        CloudEvent cloudEvent = CloudEvent.from(event);
        String json = cloudEvent.toJson();

        log.info("Publishing event to {}: type={}, id={}, correlationId={}",
            KafkaConfiguration.TRANSACTIONS_TOPIC, cloudEvent.type(), cloudEvent.id(), extractCorrelationId(event));

        return kafkaTemplate.send(KafkaConfiguration.TRANSACTIONS_TOPIC, cloudEvent.id(), json)
            .<Void>thenApply(result -> {
                log.debug("Published event {} to {} at offset {}", cloudEvent.id(),
                    KafkaConfiguration.TRANSACTIONS_TOPIC, result.getRecordMetadata().offset());
                return null;
            })
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event {} to {}: {}", cloudEvent.id(),
                        KafkaConfiguration.TRANSACTIONS_TOPIC, ex.getMessage(), ex);
                }
            });
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
