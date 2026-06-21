package com.jetledger.wallet.infrastructure.kafka;

import com.jetledger.wallet.domain.event.*;
import com.jetledger.wallet.domain.model.Money;
import java.time.Instant;
import java.util.UUID;

public record CloudEvent(
    String specversion,
    String type,
    String source,
    String id,
    String time,
    String datacontenttype,
    String data
) {

    private static final String SPEC_VERSION = "1.0";
    private static final String SOURCE = "/wallet-core";
    private static final String CONTENT_TYPE = "application/json";

    public static CloudEvent from(WalletDomainEvent event) {
        String type;
        String data;

        switch (event) {
            case WalletCreated e -> {
                type = "com.jetledger.wallet.created";
                data = """
                    {"walletId":"%s","ownerId":"%s","currency":"%s","timestamp":"%s"}
                    """.formatted(e.walletId().value(), e.ownerId().value(), e.currency().getCurrencyCode(), e.timestamp());
            }
            case MoneyDeposited e -> {
                type = "com.jetledger.wallet.deposited";
                data = """
                    {"walletId":"%s","amount":"%s","currency":"%s","balanceAfter":"%s","correlationId":"%s","timestamp":"%s"}
                    """.formatted(
                        e.walletId().value(), e.amount().amount().toPlainString(),
                        e.amount().currency().getCurrencyCode(), e.balanceAfter().amount().toPlainString(),
                        e.correlationId(), e.timestamp());
            }
            case MoneyWithdrawn e -> {
                type = "com.jetledger.wallet.withdrawn";
                data = """
                    {"walletId":"%s","amount":"%s","currency":"%s","balanceAfter":"%s","correlationId":"%s","timestamp":"%s"}
                    """.formatted(
                        e.walletId().value(), e.amount().amount().toPlainString(),
                        e.amount().currency().getCurrencyCode(), e.balanceAfter().amount().toPlainString(),
                        e.correlationId(), e.timestamp());
            }
            case WithdrawRejected e -> {
                type = "com.jetledger.wallet.withdraw.rejected";
                data = """
                    {"walletId":"%s","amount":"%s","currency":"%s","balance":"%s","reason":"%s","correlationId":"%s","timestamp":"%s"}
                    """.formatted(
                        e.walletId().value(), e.amount().amount().toPlainString(),
                        e.amount().currency().getCurrencyCode(), e.balance().amount().toPlainString(),
                        e.reason(), e.correlationId(), e.timestamp());
            }
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        }

        return new CloudEvent(
            SPEC_VERSION, type, SOURCE, UUID.randomUUID().toString(),
            Instant.now().toString(), CONTENT_TYPE, data
        );
    }

    public String toJson() {
        return """
            {"specversion":"%s","type":"%s","source":"%s","id":"%s","time":"%s","datacontenttype":"%s","data":%s}
            """.formatted(specversion, type, source, id, time, datacontenttype, data);
    }
}
