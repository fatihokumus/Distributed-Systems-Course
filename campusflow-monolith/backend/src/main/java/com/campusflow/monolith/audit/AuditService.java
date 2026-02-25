package com.campusflow.monolith.audit;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditService {

    private final AuditEntryRepository auditEntryRepository;

    public AuditService(AuditEntryRepository auditEntryRepository) {
        this.auditEntryRepository = auditEntryRepository;
    }

    public List<AuditEntryResponse> latest(int limit) {
        return auditEntryRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit)).stream()
                .map(entry -> new AuditEntryResponse(
                        entry.getId(),
                        entry.getEventType(),
                        entry.getEntityType(),
                        entry.getEntityId(),
                        entry.getPayload(),
                        entry.getCreatedAt()
                ))
                .toList();
    }
}
