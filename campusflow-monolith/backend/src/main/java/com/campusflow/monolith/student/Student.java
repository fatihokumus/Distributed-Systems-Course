package com.campusflow.monolith.student;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "students")
public class Student {

    @Id
    private UUID id;

    @Column(name = "student_no", nullable = false, unique = true)
    private String studentNo;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    protected Student() {
    }

    public Student(UUID id, String studentNo, String fullName) {
        this.id = id;
        this.studentNo = studentNo;
        this.fullName = fullName;
    }

    public UUID getId() {
        return id;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
