package com.jetledger.wallet.interfaces.api;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.jetledger.wallet.application.command.DepositCommand;
import com.jetledger.wallet.application.command.DepositHandler;
import com.jetledger.wallet.application.command.WithdrawCommand;
import com.jetledger.wallet.application.command.WithdrawHandler;
import com.jetledger.wallet.application.command.WithdrawRejectedException;
import com.jetledger.wallet.application.query.WalletDto;
import com.jetledger.wallet.application.query.WalletQueryService;
import com.jetledger.wallet.domain.model.Money;
import com.jetledger.wallet.domain.model.WalletId;
import com.jetledger.wallet.infrastructure.idempotency.IdempotencyRecord;
import com.jetledger.wallet.infrastructure.idempotency.IdempotencyService;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Currency;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final DepositHandler depositHandler;
    private final WithdrawHandler withdrawHandler;
    private final WalletQueryService queryService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    public WalletController(
            DepositHandler depositHandler,
            WithdrawHandler withdrawHandler,
            WalletQueryService queryService,
            IdempotencyService idempotencyService,
            ObjectMapper objectMapper) {
        this.depositHandler = depositHandler;
        this.withdrawHandler = withdrawHandler;
        this.queryService = queryService;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<String> deposit(
            @PathVariable UUID id,
            @RequestHeader(IDEMPOTENCY_HEADER) UUID idempotencyKey,
            @RequestBody DepositRequest request) {
        WalletId walletId = WalletId.from(id);
        String requestHash = hash(request);

        IdempotencyRecord existing = idempotencyService.get(idempotencyKey).orElse(null);
        if (existing != null) {
            if (!existing.requestHash().equals(requestHash)) {
                return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(serialize(new ErrorResponse("IDEMPOTENCY_CONFLICT",
                        "Idempotency key already used with different request body")));
            }
            return ResponseEntity
                .status(existing.responseStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(existing.responseBody());
        }

        WalletDto wallet = queryService.findById(walletId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        BigDecimal amount = request.amount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount must be positive");
        }

        Currency currency = Currency.getInstance(wallet.currency());
        Money money = Money.of(amount, currency);

        depositHandler.handle(new DepositCommand(walletId, money, idempotencyKey));

        WalletDto updated = queryService.findById(walletId).orElseThrow();
        WalletResponse response = WalletResponse.from(updated);
        String responseBody = serialize(response);

        idempotencyService.store(idempotencyKey, new IdempotencyRecord(
            idempotencyKey, 200, responseBody, requestHash, Instant.now()));

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseBody);
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<String> withdraw(
            @PathVariable UUID id,
            @RequestHeader(IDEMPOTENCY_HEADER) UUID idempotencyKey,
            @RequestBody WithdrawRequest request) {
        WalletId walletId = WalletId.from(id);
        String requestHash = hash(request);

        IdempotencyRecord existing = idempotencyService.get(idempotencyKey).orElse(null);
        if (existing != null) {
            if (!existing.requestHash().equals(requestHash)) {
                return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(serialize(new ErrorResponse("IDEMPOTENCY_CONFLICT",
                        "Idempotency key already used with different request body")));
            }
            return ResponseEntity
                .status(existing.responseStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(existing.responseBody());
        }

        WalletDto wallet = queryService.findById(walletId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));

        BigDecimal amount = request.amount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount must be positive");
        }

        Currency currency = Currency.getInstance(wallet.currency());
        Money money = Money.of(amount, currency);

        try {
            withdrawHandler.handle(new WithdrawCommand(walletId, money, idempotencyKey));
        } catch (WithdrawRejectedException e) {
            WalletDto current = queryService.findById(walletId).orElseThrow();
            String errorBody = serialize(new ErrorResponse("INSUFFICIENT_FUNDS",
                "Insufficient funds. Current balance: " + current.balance()));
            idempotencyService.store(idempotencyKey, new IdempotencyRecord(
                idempotencyKey, 422, errorBody, requestHash, Instant.now()));
            return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorBody);
        }

        WalletDto updated = queryService.findById(walletId).orElseThrow();
        WalletResponse response = WalletResponse.from(updated);
        String responseBody = serialize(response);

        idempotencyService.store(idempotencyKey, new IdempotencyRecord(
            idempotencyKey, 200, responseBody, requestHash, Instant.now()));

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseBody);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getWallet(@PathVariable UUID id) {
        WalletId walletId = WalletId.from(id);
        WalletDto dto = queryService.findById(walletId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found"));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(serialize(WalletResponse.from(dto)));
    }

    private static String hash(Object request) {
        try {
            byte[] bytes = request.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }
}
