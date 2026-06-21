# Jira Context: SCRUM-3

## Summary
Publish domain events to Redpanda and consume them in wallet-analytics

## Acceptance Criteria
- Events published to topic wallet.transactions.v1
- CloudEvents envelope: specversion, type, source, id, time, data
- wallet-analytics consumer with at-least-once semantics, idempotent handler
- Redpanda schema registry enforces Avro schema (defined in wallet-contracts)
- Integration test: publish event → consumer receives → read model updated
- Dead-letter topic wallet.transactions.v1.dlq for deserialization failures
- Consumer lag metric exposed via Micrometer
- wallet-analytics scaffolded as Spring Boot 4 service
