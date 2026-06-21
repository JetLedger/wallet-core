package com.jetledger.wallet.infrastructure.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import java.util.HashMap;
import java.util.Map;

@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(value = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaConfiguration {

    public static final String TRANSACTIONS_TOPIC = "wallet.transactions.v1";
    public static final String DLQ_TOPIC = "wallet.transactions.v1.dlq";

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public NewTopic transactionsTopic() {
        return TopicBuilder.name(TRANSACTIONS_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name(DLQ_TOPIC)
            .partitions(1)
            .replicas(1)
            .build();
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
