# SCRUM-2: Ordered Task Breakdown

## Task 1: Add Redis dependency
Add `spring-boot-starter-data-redis` to `build.gradle`.

## Task 2: Idempotency domain types
Create `IdempotencyKey` (record: clientKey UUID, route String, walletId WalletId — with `toCacheKey()`), `CachedResponse` (record: responseStatus, responseBody — with `toResponseEntity()`), `IdempotencyRecord` (record: idempotencyKey, responseStatus, responseBody, requestHash, createdAt), and `IdempotencyService` interface (`claim(IdempotencyKey, requestHash)`, `storeResult(IdempotencyKey, CachedResponse)`).

## Task 3: InMemoryIdempotencyService
Implement using Caffeine cache with `expireAfterWrite(ttl)`. `claim()` uses `putIfAbsent` on the underlying map as the atomic primitive — a placeholder record is inserted first, then polled briefly if another thread is mid-flight, to avoid the original get/store race.

## Task 4: RedisIdempotencyService
Implement with `StringRedisTemplate`, key prefix `idempotency:`, Jackson serialization, Redis-native TTL. `claim()` uses `setIfAbsent` (Redis SETNX) as the atomic primitive. Deserialization failures throw `IdempotencyStoreCorruptedException` (fail closed) rather than returning empty.

## Task 5: IdempotencyConfiguration
Spring `@Configuration` that conditionally creates `RedisIdempotencyService` (when `idempotency.redis.enabled=true`) or `InMemoryIdempotencyService` (default).

## Task 6: Request/Response DTOs
Create `DepositRequest` (amount), `WithdrawRequest` (amount), `WalletResponse` (from WalletDto), `ErrorResponse` (error, message).

## Task 7: GlobalExceptionHandler
`@RestControllerAdvice` handling `ResponseStatusException` (passthrough status), `IllegalArgumentException` (400), and generic `Exception` (500 with structured log).

## Task 8: WalletController — deposit endpoint
`POST /{id}/deposit`: validate `Idempotency-Key` header, check idempotency service (return cached or detect conflict), query wallet, call `DepositHandler`, store idempotency record, return response.

## Task 9: WalletController — withdraw endpoint
`POST /{id}/withdraw`: same pattern as deposit, but also catches `WithdrawRejectedException` and caches the 422 `INSUFFICIENT_FUNDS` error response under the idempotency key.

## Task 10: WalletController — get wallet endpoint
`GET /{id}`: simple query passthrough, no idempotency.

## Task 11: Idempotency configuration in application.yml
Add `idempotency.ttl: PT24H` and `idempotency.redis.enabled: false` to `src/main/resources/application.yml` and `src/test/resources/application.yml`.

## Task 12: Integration test — WalletControllerTest
Cover: successful deposit, retry with same key (cached), retry with different body (422 conflict), successful withdraw, retry with cached withdraw, insufficient funds (422), cached error on retry after insufficient funds, 404 for unknown wallet.
