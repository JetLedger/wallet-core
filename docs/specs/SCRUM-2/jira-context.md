# SCRUM-2 — Jira Context

## Summary
Implement idempotent deposit/withdraw endpoints with client-supplied idempotency keys

## Description
Deposit and withdraw operations must be safe to retry. The client sends an `Idempotency-Key` header (UUID). On duplicate submission within TTL, the original response is returned without re-executing the command. Keys stored in Redis with configurable TTL (default 24h).

## Acceptance Criteria
1. `POST /api/v1/wallets/{id}/deposit` and `/withdraw` accept `Idempotency-Key` header
2. Duplicate key within TTL → 200 with cached response, no side effects
3. Duplicate key with different payload → 422 `IDEMPOTENCY_CONFLICT`
4. Expired key treated as new request
5. Integration test covers: first call, retry (same key), conflict (same key, different amount), expired key
6. Redis key schema: `idempotency:{walletId}:{key}` with TTL stored alongside response

## Status
To Do

## Issue Type
Task

## Priority
Medium

## Assignee
Unassigned

## Reporter
Alex Shabetia
