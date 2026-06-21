package com.jetledger.wallet.infrastructure.kafka;

import com.jetledger.wallet.domain.event.MoneyDeposited;
import com.jetledger.wallet.domain.event.WalletCreated;
import com.jetledger.wallet.domain.model.Money;
import com.jetledger.wallet.domain.model.WalletId;
import com.jetledger.wallet.domain.model.OwnerId;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.kafka.enabled=false",
    "idempotency.redis.enabled=false"
})
@EmbeddedKafka(topics = { "wallet.transactions.v1" }, partitions = 1)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RedpandaEventPublisherTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    private KafkaTemplate<String, String> kafkaTemplate;
    private Consumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> producerProps = Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString(),
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
        );
        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProps));

        Map<String, Object> consumerProps = Map.of(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString(),
            ConsumerConfig.GROUP_ID_CONFIG, "test-group-" + UUID.randomUUID(),
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false
        );
        consumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>(consumerProps);
        embeddedKafka.consumeFromEmbeddedTopics(consumer, "wallet.transactions.v1");
        consumer.poll(Duration.ofMillis(500));
        consumer.seekToBeginning(consumer.assignment());
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) consumer.close();
    }

    @Test
    void shouldPublishWalletCreatedEvent() {
        WalletId walletId = WalletId.generate();
        OwnerId ownerId = OwnerId.generate();
        WalletCreated event = new WalletCreated(walletId, ownerId, Currency.getInstance("USD"), null);

        new RedpandaEventPublisher(kafkaTemplate).publish(event);

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, "wallet.transactions.v1", Duration.ofSeconds(15));
        String json = record.value();
        assertTrue(json.contains("com.jetledger.wallet.created"));
        assertTrue(json.contains(walletId.value().toString()));
    }

    @Test
    void shouldPublishMoneyDepositedEvent() {
        WalletId walletId = WalletId.generate();
        Money amount = Money.of(new BigDecimal("100.00"), Currency.getInstance("USD"));
        Money balanceAfter = Money.of(new BigDecimal("100.00"), Currency.getInstance("USD"));
        MoneyDeposited event = new MoneyDeposited(walletId, amount, balanceAfter, UUID.randomUUID(), null);

        new RedpandaEventPublisher(kafkaTemplate).publish(event);

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, "wallet.transactions.v1", Duration.ofSeconds(15));
        String json = record.value();
        assertTrue(json.contains("com.jetledger.wallet.deposited"));
        assertTrue(json.contains("\"100.00\""));
    }

    @Test
    void shouldContainCloudEventsEnvelope() {
        WalletId walletId = WalletId.generate();
        OwnerId ownerId = OwnerId.generate();
        WalletCreated event = new WalletCreated(walletId, ownerId, Currency.getInstance("USD"), null);

        new RedpandaEventPublisher(kafkaTemplate).publish(event);

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, "wallet.transactions.v1", Duration.ofSeconds(15));
        String json = record.value();
        assertTrue(json.contains("\"specversion\":\"1.0\""));
        assertTrue(json.contains("\"type\":\"com.jetledger.wallet.created\""));
        assertTrue(json.contains("\"source\":\"/wallet-core\""));
        assertTrue(json.contains("\"datacontenttype\":\"application/json\""));
        assertTrue(json.contains("\"data\""));
    }
}
