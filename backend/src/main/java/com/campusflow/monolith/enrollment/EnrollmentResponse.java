package com.campusflow.monolith.enrollment;

import java.util.UUID;

public record EnrollmentResponse(
        UUID enrollmentId,
        EnrollmentStatus status,
        String studentNo,
        String courseCode,
        String message
) {
}
