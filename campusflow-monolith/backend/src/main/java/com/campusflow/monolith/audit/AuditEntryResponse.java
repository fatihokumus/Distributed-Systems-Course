package com.campusflow.monolith.audit;

import java.time.Instant;
import java.util.UUID;

public record AuditEntryResponse(
        UUID id,
        String eventType,
        String entityType,
        UUID entityId,
        String payload,
        Instant createdAt
) {
}
