package com.jetledger.wallet.infrastructure.idempotency;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public record CachedResponse(int responseStatus, String responseBody) {

    public ResponseEntity<String> toResponseEntity() {
        return ResponseEntity
            .status(responseStatus)
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseBody);
    }
}
