# SCRUM-3: Publish domain events to Redpanda and consume them in wallet-analytics

## Summary
After each successful command, wallet-core publishes a domain event to Redpanda.
wallet-analytics consumes the transactions topic and persists a denormalized read model.

## Acceptance Criteria
1. Events published to topic `wallet.transactions.v1`
2. CloudEvents envelope format: specversion, type, source, id, time, data
3. wallet-analytics consumer with at-least-once semantics, idempotent handler
4. Redpanda schema registry enforces Avro schema (defined in wallet-contracts)
5. Integration test: publish event → consumer receives → read model updated
6. Dead-letter topic `wallet.transactions.v1.dlq` for deserialization failures
7. Consumer lag metric exposed via Micrometer
8. wallet-analytics scaffolded as Spring Boot 4 service

## Architecture
- wallet-core: RedpandaEventPublisher publishes CloudEvents-encoded domain events
- DomainEventBridge: Spring @EventListener bridges ApplicationEvents to Redpanda
- wallet-analytics: Kafka consumer with at-least-once, manual commit, idempotent dedup
- Read model: JPA `Transaction` entity with event_id unique constraint
- Dead-letter: DefaultErrorHandler routes deserialization failures to DLQ topic
- Metrics: Micrometer + Actuator + Prometheus registry exposed via /actuator/prometheus

## CloudEvents Mapping
| Domain Event | CloudEvents Type |
|---|---|
| WalletCreated | com.jetledger.wallet.created |
| MoneyDeposited | com.jetledger.wallet.deposited |
| MoneyWithdrawn | com.jetledger.wallet.withdrawn |
| WithdrawRejected | com.jetledger.wallet.withdraw.rejected |

All events use specversion `1.0`, source `/wallet-core`, datacontenttype `application/json`.

## Repos
- wallet-contracts: Avro schemas
- wallet-core: Event publishing
- wallet-analytics: Consumer + read model
