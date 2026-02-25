package com.campusflow.monolith.enrollment;

import com.campusflow.monolith.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    List<Enrollment> findByStudentOrderByCreatedAtDesc(Student student);
}
