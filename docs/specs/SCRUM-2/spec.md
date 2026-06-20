# SCRUM-2: Idempotent Deposit/Withdraw Endpoints

> **Note:** Spec generated retroactively — implementation preceded documentation due to a process gap, now fixed in implementation.md. This spec was further reconciled after a CodeRabbit review round that hardened the `IdempotencyService` to an atomic `claim()`/`storeResult()` design with composite, wallet-scoped keys — see constitution.md Principle 9.

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
- `IdempotencyRecord` fields: `idempotencyKey` (String), `responseStatus` (int), `responseBody` (String JSON), `requestHash` (SHA-256 of request + walletId), `createdAt` (Instant)
- Redis key: `idempotency:{route}:{walletId}:{clientKey}` with TTL applied at the Redis level
- Cache key composition: `IdempotencyKey.toCacheKey()` → `{route}:{walletId}:{clientKey}`
- In-memory: Caffeine cache with `expireAfterWrite(ttl)`

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
    Optional<CachedResponse> claim(IdempotencyKey key, String requestHash);
    void storeResult(IdempotencyKey key, CachedResponse response);
}
```

`claim()` is atomic — it both checks for an existing record and reserves the
key in a single operation (via `putIfAbsent` for in-memory, `SETNX` for Redis),
preventing the race condition where two concurrent requests with the same key
could both pass a separate "check" step before either stored a result.

`IdempotencyKey` is a composite of the client-supplied UUID, the route
(`"deposit"`/`"withdraw"`), and the `WalletId` — not a bare UUID. This scopes
idempotency per wallet and per operation, so the same client key reused on a
different wallet or a different route does not collide.

### Implementations

| Implementation | Backend | Atomic Primitive | When Used |
|---------------|---------|------------------|-----------|
| `RedisIdempotencyService` | Redis via `StringRedisTemplate` | `SETNX` via `setIfAbsent()` | `idempotency.redis.enabled=true` |
| `InMemoryIdempotencyService` | Caffeine cache | `putIfAbsent` on underlying `ConcurrentMap` | Default (`matchIfMissing = true`) |

### Configuration
```yaml
idempotency:
  ttl: PT24H           # ISO-8601 duration
  redis:
    enabled: false     # false → in-memory, true → Redis
```

## Request Hash
SHA-256 of `walletId + ":" + request.toString()` bytes. Includes the wallet ID in the hash input to prevent cross-wallet cache collisions when the same client key is reused on different wallets.

## Error Handling (GlobalExceptionHandler)
- `ResponseStatusException` → mapped to status code + `ErrorResponse`
- `IllegalArgumentException` → 400 `BAD_REQUEST`
- `IdempotencyStoreCorruptedException` → 500 `IDEMPOTENCY_STORE_ERROR` (Redis deserialization failures — fail closed)
- Unhandled exceptions → 500 `INTERNAL_ERROR` + structured log

## Financial Safety Checklist

| Item | Status |
|------|--------|
| Idempotency | UUID-based `Idempotency-Key` header, cached responses, conflict detection |
| Request integrity | SHA-256 hash of request body prevents mismatched reuse |
| TTL enforcement | Configurable TTL, lazy expiry for in-memory, Redis-native TTL |
| Concurrency | Caffeine for in-memory; Redis atomic ops via claim() |
| Audit trail | `IdempotencyRecord` timestamps + correlationId on domain events |
