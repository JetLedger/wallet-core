# SCRUM-2: Idempotent Deposit/Withdraw Endpoints

> **Note:** Spec generated retroactively — implementation preceded documentation due to a process gap, now fixed in implementation.md.

## Overview
Add an HTTP API layer (`POST /api/v1/wallets/{id}/deposit` and `/withdraw`) on top of the existing SCRUM-1 command handlers. Every mutating endpoint requires a client-supplied `Idempotency-Key` (UUID) header to guarantee safe retries. Idempotency records are stored in either Redis (production) or an in-memory store (development/test) with a configurable TTL.

## Affected Repo
`wallet-core` (Spring Boot 4 / Java 25, Gradle)

## API Endpoints

### `POST /api/v1/wallets/{id}/deposit`
| Aspect | Detail |
|--------|--------|
| Header | `Idempotency-Key: <UUID>` (required) |
| Body | `{"amount": "<decimal>"}` (DepositRequest) |
| 200 OK | Cached or fresh `WalletResponse` JSON |
| 400 | Missing/invalid amount |
| 404 | Wallet not found |
| 422 | Idempotency key reused with different request body → `IDEMPOTENCY_CONFLICT` |

### `POST /api/v1/wallets/{id}/withdraw`
| Aspect | Detail |
|--------|--------|
| Header | `Idempotency-Key: <UUID>` (required) |
| Body | `{"amount": "<decimal>"}` (WithdrawRequest) |
| 200 OK | Cached or fresh `WalletResponse` JSON |
| 400 | Missing/invalid amount |
| 404 | Wallet not found |
| 422 | Idempotency conflict → `IDEMPOTENCY_CONFLICT` |
| 422 | Insufficient funds → `INSUFFICIENT_FUNDS` |

### `GET /api/v1/wallets/{id}`
No idempotency; returns current wallet state.

## Idempotency Behaviour

1. **First call** — execute command, store `IdempotencyRecord` (status, body, request hash, timestamp)
2. **Duplicate key + same body** — return cached response (status + body), do NOT re-execute
3. **Duplicate key + different body** — 422 `IDEMPOTENCY_CONFLICT`
4. **Expired key** (past TTL) — treated as a new call
5. **Failed withdraw (insufficient funds)** — error response is also cached under the idempotency key

## Idempotency Key Schema
- `IdempotencyRecord` fields: `idempotencyKey` (UUID), `responseStatus` (int), `responseBody` (String JSON), `requestHash` (SHA-256 of request toString), `createdAt` (Instant)
- Redis key: `idempotency:{key}` with TTL applied at the Redis level
- In-memory: `ConcurrentHashMap<UUID, IdempotencyRecord>` with lazy TTL check on `get()`

## Request/Response DTOs

### DepositRequest
```json
{"amount": "100.00"}
```

### WithdrawRequest
```json
{"amount": "50.00"}
```

### WalletResponse
```json
{"walletId": "uuid", "balance": "150.00", "currency": "USD", "createdAt": "...", "updatedAt": "..."}
```

### ErrorResponse
```json
{"error": "IDEMPOTENCY_CONFLICT", "message": "Idempotency key already used with different request body"}
```

## IdempotencyService Interface
```java
interface IdempotencyService {
    Optional<IdempotencyRecord> get(UUID idempotencyKey);
    void store(UUID idempotencyKey, IdempotencyRecord record);
}
```

### Implementations
| Implementation | Backend | When Used |
|---------------|---------|-----------|
| `RedisIdempotencyService` | Redis via `StringRedisTemplate` | `idempotency.redis.enabled=true` |
| `InMemoryIdempotencyService` | `ConcurrentHashMap` | Default (`matchIfMissing = true`) |

### Configuration
```yaml
idempotency:
  ttl: PT24H           # ISO-8601 duration
  redis:
    enabled: false     # false → in-memory, true → Redis
```

## Request Hash
SHA-256 of `request.toString()` bytes. Used to detect body changes between calls sharing the same idempotency key.

## Error Handling (GlobalExceptionHandler)
- `ResponseStatusException` → mapped to status code + `ErrorResponse`
- `IllegalArgumentException` → 400 `BAD_REQUEST`
- Unhandled exceptions → 500 `INTERNAL_ERROR` + structured log

## Financial Safety Checklist

| Item | Status |
|------|--------|
| Idempotency | UUID-based `Idempotency-Key` header, cached responses, conflict detection |
| Request integrity | SHA-256 hash of request body prevents mismatched reuse |
| TTL enforcement | Configurable TTL, lazy expiry for in-memory, Redis-native TTL |
| Concurrency | `ConcurrentHashMap` for in-memory; Redis atomic ops |
| Audit trail | `IdempotencyRecord` timestamps + correlationId on domain events |
