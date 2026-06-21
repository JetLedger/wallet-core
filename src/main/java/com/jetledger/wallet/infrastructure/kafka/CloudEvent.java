package com.jetledger.wallet.infrastructure.kafka;

import com.jetledger.wallet.domain.event.*;
import com.jetledger.wallet.domain.model.Money;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.UUID;

public record CloudEvent(
    String specversion,
    String type,
    String source,
    String id,
    String time,
    String datacontenttype,
    JsonNode data
) {

    private static final String SPEC_VERSION = "1.0";
    private static final String SOURCE = "/wallet-core";
    private static final String CONTENT_TYPE = "application/json";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static CloudEvent from(WalletDomainEvent event) {
        String type;
        ObjectNode dataNode = MAPPER.createObjectNode();

        switch (event) {
            case WalletCreated e -> {
                type = "com.jetledger.wallet.created";
                dataNode.put("walletId", e.walletId().value().toString());
                dataNode.put("ownerId", e.ownerId().value().toString());
                dataNode.put("currency", e.currency().getCurrencyCode());
                dataNode.put("timestamp", e.timestamp().toString());
            }
            case MoneyDeposited e -> {
                type = "com.jetledger.wallet.deposited";
                dataNode.put("walletId", e.walletId().value().toString());
                dataNode.put("amount", e.amount().amount().toPlainString());
                dataNode.put("currency", e.amount().currency().getCurrencyCode());
                dataNode.put("balanceAfter", e.balanceAfter().amount().toPlainString());
                dataNode.put("correlationId", e.correlationId().toString());
                dataNode.put("timestamp", e.timestamp().toString());
            }
            case MoneyWithdrawn e -> {
                type = "com.jetledger.wallet.withdrawn";
                dataNode.put("walletId", e.walletId().value().toString());
                dataNode.put("amount", e.amount().amount().toPlainString());
                dataNode.put("currency", e.amount().currency().getCurrencyCode());
                dataNode.put("balanceAfter", e.balanceAfter().amount().toPlainString());
                dataNode.put("correlationId", e.correlationId().toString());
                dataNode.put("timestamp", e.timestamp().toString());
            }
            case WithdrawRejected e -> {
                type = "com.jetledger.wallet.withdraw.rejected";
                dataNode.put("walletId", e.walletId().value().toString());
                dataNode.put("amount", e.amount().amount().toPlainString());
                dataNode.put("currency", e.amount().currency().getCurrencyCode());
                dataNode.put("balance", e.balance().amount().toPlainString());
                dataNode.put("reason", e.reason());
                dataNode.put("correlationId", e.correlationId().toString());
                dataNode.put("timestamp", e.timestamp().toString());
            }
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass().getSimpleName());
        }

        return new CloudEvent(
            SPEC_VERSION, type, SOURCE, UUID.randomUUID().toString(),
            Instant.now().toString(), CONTENT_TYPE, dataNode
        );
    }

    public String toJson() {
        try {
            ObjectNode root = MAPPER.createObjectNode();
            root.put("specversion", specversion);
            root.put("type", type);
            root.put("source", source);
            root.put("id", id);
            root.put("time", time);
            root.put("datacontenttype", datacontenttype);
            root.set("data", data);
            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize CloudEvent", e);
        }
    }
}
