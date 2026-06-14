# SCRUM-1: Technical Architecture Plan

## Architecture Overview
Standard DDD layering with CQRS pattern for wallet-core. No event store needed in Sprint 1 — domain events are published via an in-memory Spring `ApplicationEventPublisher` and logged.

## Component Diagram

```
┌─────────────────────────────────────────────────────┐
│                   Application Layer                  │
│  ┌─────────────────┐  ┌──────────────────────────┐   │
│  │ CommandHandlers  │  │  WalletQueryService      │   │
│  │  - CreateWallet  │  │  - findById(): WalletDto │   │
│  │  - Deposit       │  │                          │   │
│  │  - Withdraw      │  │                          │   │
│  └────────┬────────┘  └──────────┬────────────────┘   │
│           │                      │                     │
│           ▼                      ▼                     │
│  ┌─────────────────────────────────────────────┐       │
│  │              Domain Layer                    │       │
│  │  ┌──────────┐ ┌──────────┐ ┌─────────────┐  │       │
│  │  │ Wallet   │ │ VOs      │ │ DomainEvents │  │       │
│  │  │ Aggregate│ │ WalletId,│ │ WalletCreated│  │       │
│  │  │          │ │ OwnerId, │ │ MoneyDeposited│ │       │
│  │  │          │ │ Money    │ │ MoneyWithdrawn│ │       │
│  │  └─────┬────┘ └──────────┘ │ WithdrawRej. │  │       │
│  │        │                   └──────┬────────┘  │       │
│  │        │                          │            │       │
│  │   ┌────▼────┐                     │            │       │
│  │   │WalletRepo│ (interface only)    │            │       │
│  │   └─────────┘                     │            │       │
│  └───────────────────────────────────┼────────────┘       │
│                                      │                     │
└──────────────────────────────────────┼─────────────────────┘
                                       │
              ┌────────────────────────┘
              ▼
┌──────────────────────────────────────┐
│          Infrastructure Layer          │
│  ┌────────────┐  ┌──────────────────┐ │
│  │WalletRepo  │  │WalletProjection  │ │
│  │Impl (JPA)  │  │(JPA entity +     │ │
│  │            │  │ Spring Data)     │ │
│  └────────────┘  └──────────────────┘ │
│                                        │
│  ┌──────────────────────────────────┐ │
│  │  EventPublisher (Spring Events)  │ │
│  └──────────────────────────────────┘ │
└──────────────────────────────────────┘
```

## Package Structure
```
com.jetledger.wallet
├── domain/
│   ├── model/
│   │   ├── Wallet.java
│   │   ├── WalletId.java
│   │   ├── OwnerId.java
│   │   └── Money.java
│   ├── event/
│   │   ├── WalletDomainEvent.java (interface)
│   │   ├── WalletCreated.java
│   │   ├── MoneyDeposited.java
│   │   ├── MoneyWithdrawn.java
│   │   └── WithdrawRejected.java
│   └── repository/
│       └── WalletRepository.java
├── application/
│   ├── command/
│   │   ├── CreateWalletCommand.java
│   │   ├── DepositCommand.java
│   │   ├── WithdrawCommand.java
│   │   ├── CreateWalletHandler.java
│   │   ├── DepositHandler.java
│   │   └── WithdrawHandler.java
│   └── query/
│       ├── WalletQueryService.java
│       └── WalletDto.java
└── infrastructure/
    ├── persistence/
    │   ├── JpaWalletRepository.java
    │   ├── WalletEntity.java
    │   └── WalletProjection.java
    └── event/
        └── SpringDomainEventPublisher.java
```

## Data Storage
- JPA entities in infrastructure layer (wallet table with columns: id, owner_id, balance, currency, version, created_at, updated_at)
- Optimistic locking via `@Version` on JPA entity
- Read model uses same table for Sprint 1 (separate projection table deferred to Sprint 2+)

## Dependencies
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- H2 (test) / PostgreSQL (production)
- ArchUnit (test only)
- Lombok (optional — explicit constructors preferred for DDD clarity)
