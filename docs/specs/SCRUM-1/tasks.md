# SCRUM-1: Ordered Task Breakdown

## Task 1: Initialize Gradle project structure
Create `build.gradle` with Spring Boot 4 / Java 25, dependencies (JPA, Web, H2, ArchUnit), and standard directory layout under `src/main/java/com/jetledger/wallet/` and `src/test/java/com/jetledger/wallet/`.

## Task 2: Value Objects
Implement `WalletId`, `OwnerId` (UUID wrappers) and `Money` (BigDecimal with scale 2, currency enforcement, arithmetic operations).

## Task 3: Domain Events
Implement event interface `WalletDomainEvent` and concrete events: `WalletCreated`, `MoneyDeposited`, `MoneyWithdrawn`, `WithdrawRejected`. Each includes correlationId, timestamp, and relevant fields.

## Task 4: Wallet Aggregate Root
Implement `Wallet` aggregate with:
- Fields: id, ownerId, balance, currency, version
- Methods: `create()`, `deposit()`, `withdraw()` returning events
- Invariants: non-negative balance, currency consistency, positive amounts

## Task 5: Repository Interface
Define `WalletRepository` interface in domain layer with `save()` and `findById()`.

## Task 6: Command Handlers
Implement command objects and handlers:
- `CreateWalletHandler` → validates input, calls Wallet.create(), persists, publishes WalletCreated
- `DepositHandler` → loads wallet, calls deposit(), persists, publishes MoneyDeposited
- `WithdrawHandler` → loads wallet, calls withdraw(), persists, publishes MoneyWithdrawn or WithdrawRejected

## Task 7: Query Side
Implement `WalletDto` read model and `WalletQueryService` interface. Implement projection in infrastructure.

## Task 8: Infrastructure Layer
Implement `JpaWalletRepository` (Spring Data JPA adapter for WalletRepository), `WalletEntity` (JPA entity with @Version), `WalletProjection` (JPA entity for read side), and `SpringDomainEventPublisher`.

## Task 9: ArchUnit Test
Write ArchUnit test asserting domain layer has zero imports from `infrastructure.*`.

## Task 10: Integration Wiring
Configure Spring beans, `application.yml`, base package scan, ensure end-to-end flow works.
