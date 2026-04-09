package com.campusflow.capacity;

public record CapacityCourseResponse(
        String courseCode,
        int capacity,
        int enrolledCount
) {
}