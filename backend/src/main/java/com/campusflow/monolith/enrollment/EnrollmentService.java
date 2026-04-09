package com.campusflow.monolith.enrollment;

import com.campusflow.monolith.audit.AuditEntry;
import com.campusflow.monolith.audit.AuditEntryRepository;
import com.campusflow.monolith.capacityclient.CapacityClient;
import com.campusflow.monolith.capacityclient.CapacityUnavailableException;
import com.campusflow.monolith.deadline.DeadlineContext;
import com.campusflow.monolith.deadline.DeadlineExceededException;
import com.campusflow.monolith.deadline.DeadlineHolder;
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
    private final EnrollmentRepository enrollmentRepository;
    private final AuditEntryRepository auditEntryRepository;
    private final CapacityClient capacityClient;

    public EnrollmentService(StudentRepository studentRepository,
                             EnrollmentRepository enrollmentRepository,
                             AuditEntryRepository auditEntryRepository,
                             CapacityClient capacityClient) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.auditEntryRepository = auditEntryRepository;
        this.capacityClient = capacityClient;
    }

    @Transactional
    public EnrollmentResponse enroll(EnrollmentRequest request, String correlationId) {
        checkDeadline();

        String effectiveCorrelationId =
                (correlationId == null || correlationId.isBlank())
                        ? UUID.randomUUID().toString()
                        : correlationId;

        log.info("[ID: {}] Enrollment started for studentNo={}, courseCode={}",
                effectiveCorrelationId, request.studentNo(), request.courseCode());

        Student student = studentRepository.findByStudentNo(request.studentNo())
                .orElseGet(() -> studentRepository.save(
                        new Student(UUID.randomUUID(), request.studentNo(), "Unknown")
                ));

        sleep(700);
        checkDeadline();

        boolean hasCapacity = capacityClient.hasCapacity(
                request.courseCode(),
                effectiveCorrelationId
        );

        if (!hasCapacity) {
            log.info("[ID: {}] Capacity unavailable/full or circuit breaker fallback for courseCode={}",
                    effectiveCorrelationId, request.courseCode());

            return saveEnrollmentAndAudit(
                    student,
                    request,
                    EnrollmentStatus.REJECTED,
                    "Capacity unavailable or course is full."
            );
        }

        try {
            capacityClient.increase(
                    request.courseCode(),
                    effectiveCorrelationId
            );
        } catch (CapacityUnavailableException e) {
            log.warn("[ID: {}] Capacity increase failed in degraded mode for courseCode={}",
                    effectiveCorrelationId, request.courseCode());

            return saveEnrollmentAndAudit(
                    student,
                    request,
                    EnrollmentStatus.REJECTED,
                    "Capacity service unavailable during confirmation."
            );
        }

        sleep(700);
        checkDeadline();

        log.info("[ID: {}] Enrollment confirmed for studentNo={}, courseCode={}",
                effectiveCorrelationId, request.studentNo(), request.courseCode());

        return saveEnrollmentAndAudit(
                student,
                request,
                EnrollmentStatus.CONFIRMED,
                "Enrollment confirmed."
        );
    }

    private EnrollmentResponse saveEnrollmentAndAudit(Student student,
                                                      EnrollmentRequest request,
                                                      EnrollmentStatus status,
                                                      String message) {
        Enrollment enrollment = enrollmentRepository.save(
                new Enrollment(
                        UUID.randomUUID(),
                        student,
                        request.courseCode(),
                        status,
                        Instant.now()
                )
        );

        String eventType = (status == EnrollmentStatus.CONFIRMED)
                ? "ENROLLMENT_CONFIRMED"
                : "ENROLLMENT_REJECTED";

        auditEntryRepository.save(new AuditEntry(
                UUID.randomUUID(),
                eventType,
                "Enrollment",
                enrollment.getId(),
                payloadFor(request.studentNo(), request.courseCode(), status.name()),
                Instant.now()
        ));

        return new EnrollmentResponse(
                enrollment.getId(),
                status,
                request.studentNo(),
                request.courseCode(),
                message
        );
    }

    private void checkDeadline() {
        DeadlineContext context = DeadlineHolder.get();
        if (context != null && context.isExpired()) {
            throw new DeadlineExceededException("Request deadline exceeded");
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String payloadFor(String studentNo, String courseCode, String status) {
        return "{\"studentNo\":\"" + studentNo +
                "\",\"courseCode\":\"" + courseCode +
                "\",\"status\":\"" + status + "\"}";
    }

    @Transactional
    public List<StudentEnrollmentResponse> getEnrollmentsByStudentNo(String studentNo) {
        return studentRepository.findByStudentNo(studentNo)
                .map(student -> enrollmentRepository.findByStudentOrderByCreatedAtDesc(student).stream()
                        .map(enrollment -> new StudentEnrollmentResponse(
                                enrollment.getId(),
                                enrollment.getCourseCode(),
                                enrollment.getStatus(),
                                enrollment.getCreatedAt()
                        ))
                        .toList())
                .orElse(List.of());
    }
}