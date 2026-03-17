package com.campusflow.monolith.enrollment;
import com.campusflow.monolith.deadline.DeadlineContext;
import com.campusflow.monolith.deadline.DeadlineExceededException;
import com.campusflow.monolith.deadline.DeadlineHolder;
import com.campusflow.monolith.audit.AuditEntry;
import com.campusflow.monolith.audit.AuditEntryRepository;
import com.campusflow.monolith.common.CourseNotFoundException;
import com.campusflow.monolith.course.Course;
import com.campusflow.monolith.course.CourseRepository;
import com.campusflow.monolith.student.Student;
import com.campusflow.monolith.student.StudentRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AuditEntryRepository auditEntryRepository;

    public EnrollmentService(StudentRepository studentRepository,
                             CourseRepository courseRepository,
                             EnrollmentRepository enrollmentRepository,
                             AuditEntryRepository auditEntryRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.auditEntryRepository = auditEntryRepository;
    }
private void checkDeadline() {
    DeadlineContext context = DeadlineHolder.get();

    if (context == null) {
        return;
    }

    if (context.isExpired()) {
        throw new DeadlineExceededException("Request deadline exceeded");
    }
}

private long remainingMs() {
    DeadlineContext context = DeadlineHolder.get();

    if (context == null) {
        return Long.MAX_VALUE;
    }

    return context.getRemainingTimeMs();
}

private void sleep(long ms) {
    try {
        Thread.sleep(ms);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
    }
}
    @Transactional
    
public EnrollmentResponse enroll(EnrollmentRequest request) {
    checkDeadline();
    System.out.println("Step 1 - request start, remaining=" + remainingMs());

    Student student = studentRepository.findByStudentNo(request.studentNo())
            .orElseGet(() -> studentRepository.save(
                    new Student(UUID.randomUUID(), request.studentNo(), "Unknown")
            ));

    sleep(700);
    checkDeadline();
    System.out.println("Step 2 - after student lookup/save, remaining=" + remainingMs());

    Course course = courseRepository.findByCodeForUpdate(request.courseCode())
            .orElseThrow(() -> new CourseNotFoundException(request.courseCode()));

    sleep(700);
    checkDeadline();
    System.out.println("Step 3 - after course lookup, remaining=" + remainingMs());

    boolean confirmed = course.getEnrolledCount() < course.getCapacity();
    EnrollmentStatus status = confirmed ? EnrollmentStatus.CONFIRMED : EnrollmentStatus.REJECTED;

    if (confirmed) {
        course.setEnrolledCount(course.getEnrolledCount() + 1);
        courseRepository.save(course);
    }

    sleep(700);
    checkDeadline();
    System.out.println("Step 4 - after capacity check/update, remaining=" + remainingMs());

    Enrollment enrollment = enrollmentRepository.save(
            new Enrollment(UUID.randomUUID(), student, course, status, Instant.now())
    );

    checkDeadline();
    System.out.println("Step 5 - after enrollment save, remaining=" + remainingMs());

    String eventType = confirmed ? "ENROLLMENT_CONFIRMED" : "ENROLLMENT_REJECTED";
    String message = confirmed ? "Enrollment confirmed." : "Course capacity is full.";
    String payload = payloadFor(request.studentNo(), request.courseCode(), status.name());

    auditEntryRepository.save(new AuditEntry(
            UUID.randomUUID(),
            eventType,
            "Enrollment",
            enrollment.getId(),
            payload,
            Instant.now()
    ));

    checkDeadline();
    System.out.println("Step 6 - after audit save, remaining=" + remainingMs());

    log.info("Enrollment attempt studentNo={}, courseCode={}, result={}",
            request.studentNo(), request.courseCode(), status);

    return new EnrollmentResponse(
            enrollment.getId(),
            status,
            request.studentNo(),
            request.courseCode(),
            message
    );
}

    @Transactional
    public List<StudentEnrollmentResponse> getEnrollmentsByStudentNo(String studentNo) {
        return studentRepository.findByStudentNo(studentNo)
                .map(student -> enrollmentRepository.findByStudentOrderByCreatedAtDesc(student).stream()
                        .map(enrollment -> new StudentEnrollmentResponse(
                                enrollment.getId(),
                                enrollment.getCourse().getCode(),
                                enrollment.getStatus(),
                                enrollment.getCreatedAt()
                        ))
                        .toList())
                .orElse(List.of());
    }

    private String payloadFor(String studentNo, String courseCode, String status) {
        return "{\"studentNo\":\"" + studentNo + "\",\"courseCode\":\"" + courseCode + "\",\"status\":\"" + status + "\"}";
    }

}
