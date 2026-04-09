package com.campusflow.monolith.capacityclient;

public record CapacityCourseResponse(
        String courseCode,
        int capacity,
        int enrolledCount
) {
}