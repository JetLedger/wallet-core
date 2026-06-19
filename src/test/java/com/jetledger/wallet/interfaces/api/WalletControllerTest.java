package com.jetledger.wallet.interfaces.api;

import tools.jackson.databind.ObjectMapper;
import com.jetledger.wallet.application.command.CreateWalletCommand;
import com.jetledger.wallet.application.command.CreateWalletHandler;
import com.jetledger.wallet.domain.model.OwnerId;
import com.jetledger.wallet.domain.model.WalletId;
import com.jetledger.wallet.domain.repository.WalletRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Currency;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WalletControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient http = HttpClient.newHttpClient();

    private UUID walletId;

    @BeforeEach
    void setUp() {
        CreateWalletHandler handler = new CreateWalletHandler(walletRepository);
        WalletId id = handler.handle(new CreateWalletCommand(
            OwnerId.generate(), Currency.getInstance("USD"), UUID.randomUUID()));
        walletId = id.value();
    }

    @Test
    void shouldDepositSuccessfully() throws Exception {
        UUID idempotencyKey = UUID.randomUUID();
        WalletResponse response = deposit(idempotencyKey, "100.00");
        assertEquals(new java.math.BigDecimal("100.00"), response.balance());
    }

    @Test
    void shouldReturnCachedResponseOnRetryWithSameKey() throws Exception {
        UUID idempotencyKey = UUID.randomUUID();
        WalletResponse first = deposit(idempotencyKey, "100.00");
        assertEquals(new java.math.BigDecimal("100.00"), first.balance());

        WalletResponse second = deposit(idempotencyKey, "100.00");
        assertEquals(new java.math.BigDecimal("100.00"), second.balance());
    }

    @Test
    void shouldReturn422OnIdempotencyConflict() throws Exception {
        UUID idempotencyKey = UUID.randomUUID();
        deposit(idempotencyKey, "100.00");

        ErrorResponse conflict = depositAndExpectError(idempotencyKey, "200.00");
        assertEquals("IDEMPOTENCY_CONFLICT", conflict.error());
    }

    @Test
    void shouldWithdrawSuccessfully() throws Exception {
        deposit(UUID.randomUUID(), "200.00");
        UUID idempotencyKey = UUID.randomUUID();
        WalletResponse response = withdraw(idempotencyKey, "50.00");
        assertEquals(new java.math.BigDecimal("150.00"), response.balance());
    }

    @Test
    void shouldReturnCachedWithdrawResponseOnRetry() throws Exception {
        deposit(UUID.randomUUID(), "200.00");
        UUID idempotencyKey = UUID.randomUUID();
        WalletResponse first = withdraw(idempotencyKey, "50.00");
        assertEquals(new java.math.BigDecimal("150.00"), first.balance());

        WalletResponse second = withdraw(idempotencyKey, "50.00");
        assertEquals(new java.math.BigDecimal("150.00"), second.balance());
    }

    @Test
    void shouldReturn422OnInsufficientFunds() throws Exception {
        deposit(UUID.randomUUID(), "30.00");
        UUID idempotencyKey = UUID.randomUUID();
        ErrorResponse error = withdrawAndExpectError(idempotencyKey, "100.00");
        assertEquals("INSUFFICIENT_FUNDS", error.error());
    }

    @Test
    void shouldReturnCachedErrorOnRetryAfterInsufficientFunds() throws Exception {
        deposit(UUID.randomUUID(), "30.00");
        UUID idempotencyKey = UUID.randomUUID();
        ErrorResponse first = withdrawAndExpectError(idempotencyKey, "100.00");
        assertEquals("INSUFFICIENT_FUNDS", first.error());

        ErrorResponse second = withdrawAndExpectError(idempotencyKey, "100.00");
        assertEquals("INSUFFICIENT_FUNDS", second.error());
    }

    @Test
    void shouldReturn404ForUnknownWallet() throws Exception {
        UUID unknownId = UUID.randomUUID();
        UUID idempotencyKey = UUID.randomUUID();
        String body = "{\"amount\":\"100.00\"}";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/v1/wallets/" + unknownId + "/deposit"))
            .header("Content-Type", "application/json")
            .header("Idempotency-Key", idempotencyKey.toString())
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldGetWalletById() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/v1/wallets/" + walletId))
            .GET()
            .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        WalletResponse wallet = objectMapper.readValue(response.body(), WalletResponse.class);
        assertEquals(walletId, wallet.walletId());
    }

    private WalletResponse deposit(UUID idempotencyKey, String amount) throws Exception {
        String body = "{\"amount\":\"" + amount + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/v1/wallets/" + walletId + "/deposit"))
            .header("Content-Type", "application/json")
            .header("Idempotency-Key", idempotencyKey.toString())
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        return objectMapper.readValue(response.body(), WalletResponse.class);
    }

    private ErrorResponse depositAndExpectError(UUID idempotencyKey, String amount) throws Exception {
        String body = "{\"amount\":\"" + amount + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/v1/wallets/" + walletId + "/deposit"))
            .header("Content-Type", "application/json")
            .header("Idempotency-Key", idempotencyKey.toString())
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(422, response.statusCode());
        return objectMapper.readValue(response.body(), ErrorResponse.class);
    }

    private WalletResponse withdraw(UUID idempotencyKey, String amount) throws Exception {
        String body = "{\"amount\":\"" + amount + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/v1/wallets/" + walletId + "/withdraw"))
            .header("Content-Type", "application/json")
            .header("Idempotency-Key", idempotencyKey.toString())
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        return objectMapper.readValue(response.body(), WalletResponse.class);
    }

    private ErrorResponse withdrawAndExpectError(UUID idempotencyKey, String amount) throws Exception {
        String body = "{\"amount\":\"" + amount + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:" + port + "/api/v1/wallets/" + walletId + "/withdraw"))
            .header("Content-Type", "application/json")
            .header("Idempotency-Key", idempotencyKey.toString())
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(422, response.statusCode());
        return objectMapper.readValue(response.body(), ErrorResponse.class);
    }
}
