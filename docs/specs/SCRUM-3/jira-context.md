# Jira Context: SCRUM-3

> Scope: wallet-core only — see wallet-analytics' own jira-context.md for consumer-side criteria.

## Summary
Publish domain events to Redpanda from wallet-core

## Acceptance Criteria
- Events published to topic `wallet.transactions.v1`
- CloudEvents envelope format: specversion, type, source, id, time, data
- Redpanda schema registry enforces Avro schema (defined in wallet-contracts)
- Integration test: publish event → consumer receives → read model updated
- Dead-letter topic `wallet.transactions.v1.dlq` for deserialization failures
