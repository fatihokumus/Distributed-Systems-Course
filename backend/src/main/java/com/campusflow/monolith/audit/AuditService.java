package com.campusflow.monolith.audit;

import java.util.List;

public interface AuditService {

    void logEnrollment(String studentNo, String courseCode, String status, String entityId);

    List<AuditEntryResponse> latest(int limit);
}