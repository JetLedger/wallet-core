# SCRUM-3 Implementation Notes

## Architecture

- wallet-core publishes domain events to Redpanda via `RedpandaEventPublisher`
- `DomainEventBridge` uses `@TransactionalEventListener(AFTER_COMMIT)` to publish events only after successful transaction commit
- Events are encoded as CloudEvents 1.0 JSON and sent to topic `wallet.transactions.v1`
- `EventPublishException` wraps Kafka send failures

## Known Limitations

### Event publishing is fire-and-forget after commit

A temporary Kafka outage between transaction commit and the asynchronous publish
results in a logged failure with no automatic retry. The database write has
already committed, so the event is lost in that scenario.

**Recommended future improvement:** Implement the outbox pattern — write events
to a database outbox table within the same transaction, then a separate polling
publisher reads from the outbox and sends to Kafka with at-least-once
guarantees. This is a separate architectural change and is tracked as a future
enhancement.
