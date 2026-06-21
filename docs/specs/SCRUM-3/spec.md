# SCRUM-3: Publish domain events to Redpanda from wallet-core

## Summary
After each successful command, wallet-core publishes a domain event to Redpanda.

## Acceptance Criteria
1. Events published to topic `wallet.transactions.v1`
2. CloudEvents envelope format: specversion, type, source, id, time, data
3. Redpanda schema registry enforces Avro schema (defined in wallet-contracts)
4. Integration test: publish event → consumer receives → read model updated
5. Dead-letter topic `wallet.transactions.v1.dlq` for deserialization failures

## Architecture
- wallet-core: RedpandaEventPublisher publishes CloudEvents-encoded domain events
- DomainEventBridge: Spring @TransactionalEventListener bridges ApplicationEvents to Redpanda after commit
- Dead-letter: RedpandaEventPublisher routes deserialization failures to DLQ topic

## CloudEvents Mapping
| Domain Event | CloudEvents Type |
|---|---|
| WalletCreated | com.jetledger.wallet.created |
| MoneyDeposited | com.jetledger.wallet.deposited |
| MoneyWithdrawn | com.jetledger.wallet.withdrawn |
| WithdrawRejected | com.jetledger.wallet.withdraw.rejected |

All events use specversion `1.0`, source `/wallet-core`, datacontenttype `application/json`.

## Repos
- wallet-contracts: Avro schemas (separate repo)
- wallet-core: Event publishing
