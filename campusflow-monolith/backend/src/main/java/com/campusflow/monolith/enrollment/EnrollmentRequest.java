package com.campusflow.monolith.enrollment;

import jakarta.validation.constraints.NotBlank;

public record EnrollmentRequest(
        @NotBlank(message = "studentNo is required") String studentNo,
        @NotBlank(message = "courseCode is required") String courseCode
) {
}
