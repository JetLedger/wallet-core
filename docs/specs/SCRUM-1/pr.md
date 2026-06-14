# [SCRUM-1] Create Wallet aggregate with DDD model and CQRS command/query split

## Summary
Implement the core `Wallet` aggregate as a DDD entity in `wallet-core` with CQRS pattern. Commands and queries are strictly separated. All invariants enforced inside the aggregate.

## Changes

### Domain Layer
- `WalletId`, `OwnerId`, `Money` value objects
- `Wallet` aggregate root with deposit/withdraw, domain events, optimistic locking version
- `WalletCreated`, `MoneyDeposited`, `MoneyWithdrawn`, `WithdrawRejected` domain events
- `WalletRepository` interface

### Application Layer
- `CreateWalletCommand` / `CreateWalletHandler`
- `DepositCommand` / `DepositHandler`
- `WithdrawCommand` / `WithdrawHandler`
- `WalletQueryService` / `WalletDto` / `WalletProjection` (read side)

### Infrastructure Layer
- JPA entities with `@Version` for optimistic locking
- H2 in-memory database (dev), PostgreSQL ready (prod)
- Spring Domain Event Publisher with structured logging

### Tests
- 54 unit tests across 8 test files
- ArchUnit test verifying domain layer has zero infrastructure imports
- In-memory stubs for handlers and query service

## Financial Safety
- **Idempotency**: CorrelationId on commands
- **Concurrency**: Optimistic locking via `@Version`
- **Precision**: BigDecimal scale 2
- **Audit**: correlationId on every event
- **Rollback**: N/A (Sprint 1)

## Validation
- Build: ✓
- Unit tests: 54/54 ✓
- ArchUnit: ✓
