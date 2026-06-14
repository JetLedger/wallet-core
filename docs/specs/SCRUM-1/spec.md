# SCRUM-1: Wallet Aggregate with DDD Model and CQRS

## Overview
Implement the core `Wallet` aggregate as a DDD entity in `wallet-core`. The aggregate enforces all invariants internally. Commands and queries are strictly separated (CQRS). No direct DB access from domain layer.

## Affected Repo
`wallet-core` only (Spring Boot 4 / Java 25, Gradle)

## Domain Model

### Value Objects
- **WalletId** — `UUID` wrapper, identity of the aggregate
- **OwnerId** — `UUID` wrapper, identifies the wallet owner
- **Money** — `BigDecimal` with scale 2, currency code (ISO 4217), encapsulated arithmetic

### Aggregate Root: `Wallet`
| Field | Type | Notes |
|-------|------|-------|
| id | WalletId | Immutable, set at creation |
| ownerId | OwnerId | Immutable, set at creation |
| balance | Money | Mutable, enforced non-negative |
| currency | Currency | ISO 4217, set at creation |
| version | long | Optimistic lock version |

### Invariants (enforced in aggregate)
1. Balance must never go negative
2. All operations on a wallet use the same currency (set at creation)
3. Withdraw amount must be positive and ≤ balance

## CQRS — Commands

| Command | Handler | Effect |
|---------|---------|--------|
| `CreateWalletCommand` (ownerId, currency) | CreateWalletHandler | Creates Wallet, emits WalletCreated |
| `DepositCommand` (walletId, amount, correlationId) | DepositHandler | Adds to balance, emits MoneyDeposited |
| `WithdrawCommand` (walletId, amount, correlationId) | WithdrawHandler | Deducts from balance, emits MoneyWithdrawn or WithdrawRejected |

## CQRS — Queries

- `WalletQueryService` — interface with `findById(WalletId): WalletDto`
- `WalletDto` — read-only DTO: id, ownerId, balance, currency, createdAt, updatedAt
- Implementation uses a database projection, never the aggregate itself

## Domain Events

| Event | Raised When | Fields |
|-------|------------|--------|
| `WalletCreated` | Wallet created | walletId, ownerId, currency, timestamp |
| `MoneyDeposited` | Deposit succeeds | walletId, amount, balanceAfter, correlationId, timestamp |
| `MoneyWithdrawn` | Withdraw succeeds | walletId, amount, balanceAfter, correlationId, timestamp |
| `WithdrawRejected` | Withdraw fails (insufficient funds) | walletId, amount, balance, reason, correlationId, timestamp |

## Architecture Constraints (DDD Layers)

```
domain/        — Wallet aggregate, value objects, domain events, repository interface
application/   — Command handlers, query service interface
infrastructure/ — Repository impl, DB projection, event bus
```

- Domain layer: ZERO imports from `infrastructure.*` (enforced by ArchUnit)
- Application layer: depends on domain only
- Infrastructure layer: implements domain interfaces

## Financial Safety Checklist (Constitution Principle 5)

| Item | Status |
|------|--------|
| Idempotency | CorrelationId on commands prevents double-processing |
| Concurrency | Optimistic locking via version field on Wallet |
| Currency precision | BigDecimal scale 2, no float/double |
| Audit trail | correlationId + userId on every event |
| Rollback safety | Not applicable (Sprint 1 — no migrations yet) |

## Observability (Sprint 0-1)
Structured logs on every command execution with correlationId. No OTel metrics yet.

## Open Questions
None. Acceptance criteria are unambiguous.
