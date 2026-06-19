# SCRUM-1 — Jira Context

## Summary
Create Wallet aggregate with DDD model and CQRS command/query split

## Description
Implement the core `Wallet` aggregate as a DDD entity in `wallet-core`. The aggregate enforces all invariants (balance non-negative, currency consistency). Commands and queries are strictly separated: write side via command handlers, read side via a dedicated projection. No direct DB access from the domain layer.

## Acceptance Criteria
1. `Wallet` aggregate root with `WalletId`, `OwnerId`, `Money` value objects
2. Commands: `CreateWalletCommand`, `DepositCommand`, `WithdrawCommand`
3. Domain events: `WalletCreated`, `MoneyDeposited`, `MoneyWithdrawn`, `WithdrawRejected`
4. ArchUnit test asserts domain layer has zero imports from `infrastructure.*`
5. `WalletQueryService` returns read model (DTO), never the aggregate itself
6. All invariants enforced inside the aggregate, not in application service

## Status
To Do

## Issue Type
Task

## Priority
Medium

## Assignee
Alex Shabetia
