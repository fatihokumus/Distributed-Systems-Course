package com.campusflow.monolith.capacity;

public interface CapacityService {

    boolean hasCapacity(String courseCode);

    void increaseEnrollment(String courseCode);
}