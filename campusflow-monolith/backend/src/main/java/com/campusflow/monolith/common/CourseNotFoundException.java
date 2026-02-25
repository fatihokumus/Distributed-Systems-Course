package com.campusflow.monolith.common;

public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(String courseCode) {
        super("Course not found: " + courseCode);
    }
}
