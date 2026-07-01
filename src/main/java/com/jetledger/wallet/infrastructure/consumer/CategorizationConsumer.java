package com.jetledger.wallet.infrastructure.consumer;

import com.jetledger.wallet.domain.categorization.TransactionCategorization;
import com.jetledger.wallet.domain.categorization.TransactionCategorizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(value = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class CategorizationConsumer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final TransactionCategorizationRepository repository;

    public CategorizationConsumer(TransactionCategorizationRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(
        topics = "${categorization.kafka.topic:wallet.categorizations.v1}",
        groupId = "${spring.kafka.consumer.group-id:wallet-core}",
        containerFactory = "categorizationKafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String eventId = record.key();
        String json = record.value();

        log.info("Received categorization event: eventId={}, topic={}, partition={}, offset={}",
            eventId, record.topic(), record.partition(), record.offset());

        if (repository.existsByEventId(eventId)) {
            log.debug("Skipping duplicate categorization: eventId={}", eventId);
            if (ack != null) ack.acknowledge();
            return;
        }

        try {
            JsonNode root = MAPPER.readTree(json);
            JsonNode data = root.path("data");

            String category = data.get("category").asText();
            double confidence = data.get("confidence").asDouble();
            String reasoning = data.has("reasoning") ? data.get("reasoning").asText() : null;
            boolean humanReviewRequired = data.path("humanReviewRequired").asBoolean(false);

            TransactionCategorization categorization = new TransactionCategorization(
                UUID.randomUUID(), eventId, category, BigDecimal.valueOf(confidence),
                reasoning, humanReviewRequired
            );

            repository.save(categorization);
            log.info("Persisted categorization: eventId={}, category={}, confidence={}",
                eventId, category, confidence);

            if (ack != null) ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process categorization event {}: {}", eventId, e.getMessage(), e);
        }
    }
}
