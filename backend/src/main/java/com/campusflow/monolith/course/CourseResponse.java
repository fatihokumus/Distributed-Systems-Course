package com.campusflow.monolith.course;

public record CourseResponse(
        String code,
        String name,
        int capacity,
        int enrolledCount,
        int remaining
) {
}
