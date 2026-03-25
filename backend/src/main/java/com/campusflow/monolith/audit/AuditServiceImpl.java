package com.campusflow.monolith.audit;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditEntryRepository auditEntryRepository;

    public AuditServiceImpl(AuditEntryRepository auditEntryRepository) {
        this.auditEntryRepository = auditEntryRepository;
    }

    @Override
    public void logEnrollment(String studentNo, String courseCode, String status, String entityId) {
        String eventType = status.equals("CONFIRMED")
                ? "ENROLLMENT_CONFIRMED"
                : "ENROLLMENT_REJECTED";

        String payload = "{\"studentNo\":\"" + studentNo +
                "\",\"courseCode\":\"" + courseCode +
                "\",\"status\":\"" + status + "\"}";

        auditEntryRepository.save(new AuditEntry(
                UUID.randomUUID(),
                eventType,
                "Enrollment",
                UUID.fromString(entityId),
                payload,
                Instant.now()
        ));
    }

    @Override
    public List<AuditEntryResponse> latest(int limit) {
        return auditEntryRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .limit(limit)
                .map(audit -> new AuditEntryResponse(
                        audit.getId(),
                        audit.getEventType(),
                        audit.getEntityType(),
                        audit.getEntityId(),
                        audit.getPayload(),
                        audit.getCreatedAt()
                ))
                .toList();
    }
}