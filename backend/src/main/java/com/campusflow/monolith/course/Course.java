package com.campusflow.monolith.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    private UUID id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "enrolled_count", nullable = false)
    private int enrolledCount;

    protected Course() {
    }

    public Course(UUID id, String code, String name, int capacity, int enrolledCount) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.capacity = capacity;
        this.enrolledCount = enrolledCount;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(int enrolledCount) {
        this.enrolledCount = enrolledCount;
    }
}
