package com.jetledger.wallet.domain.categorization;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transaction_categorizations")
public class TransactionCategorization {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(columnDefinition = "TEXT")
    private String reasoning;

    @Column(name = "human_review_required", nullable = false)
    private boolean humanReviewRequired;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TransactionCategorization() {}

    public TransactionCategorization(UUID id, String eventId, String category, BigDecimal confidence,
                                     String reasoning, boolean humanReviewRequired) {
        this.id = id;
        this.eventId = eventId;
        this.category = category;
        this.confidence = confidence;
        this.reasoning = reasoning;
        this.humanReviewRequired = humanReviewRequired;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getEventId() { return eventId; }
    public String getCategory() { return category; }
    public BigDecimal getConfidence() { return confidence; }
    public String getReasoning() { return reasoning; }
    public boolean isHumanReviewRequired() { return humanReviewRequired; }
    public Instant getCreatedAt() { return createdAt; }
}
