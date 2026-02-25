package com.campusflow.monolith.enrollment;

import java.time.Instant;
import java.util.UUID;

public record StudentEnrollmentResponse(
        UUID id,
        String courseCode,
        EnrollmentStatus status,
        Instant createdAt
) {
}
