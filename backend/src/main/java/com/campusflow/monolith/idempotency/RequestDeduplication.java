package com.campusflow.monolith.idempotency;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "request_deduplication")
public class RequestDeduplication {

    @Id
    private String requestId;

    private int responseStatus;

    private String responseBody;

    private Instant createdAt = Instant.now();

    public RequestDeduplication() {
    }

    public RequestDeduplication(String requestId, int responseStatus, String responseBody) {
        this.requestId = requestId;
        this.responseStatus = responseStatus;
        this.responseBody = responseBody;
        this.createdAt = Instant.now();
    }

    public String getRequestId() {
        return requestId;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}