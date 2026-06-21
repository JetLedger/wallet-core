# [SCRUM-3] Publish domain events to Redpanda — wallet-core

## Changes
- Added `spring-kafka` dependency to build.gradle
- `KafkaConfiguration.java` — conditional Kafka producer config with topics `wallet.transactions.v1` and `wallet.transactions.v1.dlq`
- `CloudEvent.java` — CloudEvents 1.0 envelope builder
- `RedpandaEventPublisher.java` — publishes domain events as CloudEvents JSON to Redpanda
- `DomainEventBridge.java` — `@EventListener` bridging Spring application events to Redpanda
- `JpaWalletRepository.java` — publishes domain events after persisting aggregate
- application.yml — added Kafka bootstrap-servers config
- Integration test with EmbeddedKafka verifying CloudEvents envelope format and event payloads
- Kafka disabled in test profile to not affect existing tests

## Cross-references
- wallet-contracts: https://github.com/JetLedger/wallet-contracts/pull/1
- wallet-analytics: PR URL TBD

## Validation
- 68 tests pass (including 3 new EmbeddedKafka tests)
- Events use CloudEvents 1.0 envelope: specversion, type, source, id, time, datacontenttype, data
