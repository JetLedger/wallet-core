# SCRUM-1: Implementation Summary

## Files Created

### Domain Layer (8 files)
| File | Responsibility |
|------|---------------|
| `domain/model/WalletId.java` | UUID value object |
| `domain/model/OwnerId.java` | UUID value object |
| `domain/model/Money.java` | BigDecimal + Currency value object with arithmetic |
| `domain/model/Wallet.java` | Aggregate root with deposit/withdraw, events, versioning |
| `domain/event/WalletDomainEvent.java` | Marker interface |
| `domain/event/WalletCreated.java` | Event emitted on wallet creation |
| `domain/event/MoneyDeposited.java` | Event emitted on successful deposit |
| `domain/event/MoneyWithdrawn.java` | Event emitted on successful withdrawal |
| `domain/event/WithdrawRejected.java` | Event emitted on failed withdrawal |
| `domain/repository/WalletRepository.java` | Repository interface |

### Application Layer (8 files)
| File | Responsibility |
|------|---------------|
| `application/command/CreateWalletCommand.java` | Command record |
| `application/command/DepositCommand.java` | Command record |
| `application/command/WithdrawCommand.java` | Command record |
| `application/command/CreateWalletHandler.java` | Handles creation |
| `application/command/DepositHandler.java` | Handles deposit |
| `application/command/WithdrawHandler.java` | Handles withdrawal |
| `application/command/WithdrawRejectedException.java` | Exception for rejected withdrawals |
| `application/query/WalletDto.java` | Read-only DTO |
| `application/query/WalletProjection.java` | Projection interface |
| `application/query/WalletQueryService.java` | Query service |

### Infrastructure Layer (5 files)
| File | Responsibility |
|------|---------------|
| `infrastructure/persistence/WalletEntity.java` | JPA entity with @Version |
| `infrastructure/persistence/SpringDataWalletRepository.java` | Spring Data JPA interface |
| `infrastructure/persistence/JpaWalletRepository.java` | WalletRepository adapter |
| `infrastructure/persistence/WalletProjectionImpl.java` | WalletProjection adapter |
| `infrastructure/event/SpringDomainEventPublisher.java` | Event publisher with structured logging |

### Tests (8 files)
See `tests.md` for full manifest.

### Build (3 files)
- `build.gradle` — Spring Boot 4 / Java 25, JPA, H2, ArchUnit
- `settings.gradle` — Project name
- `application.yml` — H2 in-memory datasource

## Key Decisions
- Aggressive invariants enforced inside Wallet aggregate (balance non-negative, currency consistency, positive amounts)
- WithdrawRejected does not throw — aggregate records event; command handler checks and throws
- CorrelationId auto-generated if not provided
- No Lombok — explicit constructors for DDD clarity
- WalletEntity uses Hibernate @Version for optimistic locking
