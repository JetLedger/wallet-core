# SCRUM-1: Test Manifest

## Test Files

| File | Coverage |
|------|----------|
| `src/test/java/.../domain/model/MoneyTest.java` | Money value object (creation, arithmetic, validation, equality) |
| `src/test/java/.../domain/model/WalletIdTest.java` | WalletId value object (creation, null check, equality) |
| `src/test/java/.../domain/model/OwnerIdTest.java` | OwnerId value object (creation, null check, equality) |
| `src/test/java/.../domain/model/WalletTest.java` | Wallet aggregate (creation, deposit, withdraw, invariants, events, versioning) |
| `src/test/java/.../domain/event/DomainEventsTest.java` | All 4 domain events (creation, fields, interface contract) |
| `src/test/java/.../application/command/CommandHandlerTest.java` | All 3 command handlers (happy path, insufficient funds, non-existent wallet) |
| `src/test/java/.../application/query/WalletQueryServiceTest.java` | Query service (found, not found, no aggregate leak) |
| `src/test/java/.../infrastructure/ArchitectureTest.java` | ArchUnit: domain layer has zero imports from infrastructure |

## Test Count
~50 test cases across 8 test files.

## Test Strategy
- Unit tests with no Spring context (pure Java)
- In-memory repository/projection stubs for handlers and query service
- ArchUnit for layer boundary enforcement
