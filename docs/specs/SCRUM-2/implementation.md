# SCRUM-2: Implementation Summary

> **Note:** Implementation preceded documentation due to a process gap. This document describes what was actually built.

## Files Created

### Infrastructure — Idempotency (5 files)
| File | Responsibility |
|------|---------------|
| `infrastructure/idempotency/IdempotencyService.java` | Interface: `get(UUID) → Optional<IdempotencyRecord>`, `store(UUID, IdempotencyRecord)` |
| `infrastructure/idempotency/IdempotencyRecord.java` | Record: `idempotencyKey`, `responseStatus`, `responseBody`, `requestHash`, `createdAt` |
| `infrastructure/idempotency/InMemoryIdempotencyService.java` | `ConcurrentHashMap`-backed with lazy TTL expiry |
| `infrastructure/idempotency/RedisIdempotencyService.java` | Redis-backed via `StringRedisTemplate`, key prefix `idempotency:`, Jackson serialization |
| `infrastructure/idempotency/IdempotencyConfiguration.java` | `@Configuration` choosing Redis or in-memory via `idempotency.redis.enabled` |

### Interfaces — API Layer (6 files)
| File | Responsibility |
|------|---------------|
| `interfaces/api/WalletController.java` | REST controller: `POST /{id}/deposit`, `POST /{id}/withdraw`, `GET /{id}` |
| `interfaces/api/DepositRequest.java` | `record DepositRequest(BigDecimal amount)` |
| `interfaces/api/WithdrawRequest.java` | `record WithdrawRequest(BigDecimal amount)` |
| `interfaces/api/WalletResponse.java` | `record WalletResponse(...)` with static `from(WalletDto)` factory |
| `interfaces/api/ErrorResponse.java` | `record ErrorResponse(String error, String message)` |
| `interfaces/api/GlobalExceptionHandler.java` | `@RestControllerAdvice` with 400/500/ passthrough handling |

### Tests (1 file)
| File | Responsibility |
|------|---------------|
| `interfaces/api/WalletControllerTest.java` | Integration test (8 test methods) covering deposit, withdraw, idempotency caching, conflict, insufficient funds, and 404 |

### Configuration (2 files updated)
| File | Change |
|------|--------|
| `build.gradle` | Added `spring-boot-starter-data-redis` dependency |
| `src/main/resources/application.yml` | Added `idempotency.ttl: PT24H`, `idempotency.redis.enabled: false` |
| `src/test/resources/application.yml` | Added `idempotency.ttl: PT24H`, `idempotency.redis.enabled: false` |

## Key Design Decisions

1. **Checked before command execution** — Idempotency check happens in the controller BEFORE calling the command handler, preventing any side effects on replay.
2. **Request hashing** — SHA-256 of `request.toString()` detects body changes on key reuse. Same key + different body → 422 `IDEMPOTENCY_CONFLICT`.
3. **Error caching** — Insufficient-funds errors are also cached under the idempotency key, so retries return the same 422 without re-checking balance.
4. **Controller returns raw `ResponseEntity<String>`** — Serialization is manual via `ObjectMapper` to allow caching pre-serialized response bodies in the `IdempotencyRecord`.
5. **Redis key pattern** — `idempotency:{key}` (flat, not scoped by walletId since key is already a UUID). TTL set at Redis level via `opsForValue().set(key, json, ttl)`.
6. **In-memory TTL** — Lazy check on `get()`: if the record's `createdAt + ttl` is in the past, it is evicted and treated as a miss.
7. **No Lombok in idempotency layer** — Consistent with SCRUM-1 convention. `IdempotencyRecord` is a plain Java record.

## Files Modified (existing)
| File | Change |
|------|--------|
| `build.gradle` | Added `spring-boot-starter-data-redis` compile dependency |
| `src/main/resources/application.yml` | Added `idempotency.*` configuration block |
| `src/test/resources/application.yml` | Added `idempotency.*` configuration block |
