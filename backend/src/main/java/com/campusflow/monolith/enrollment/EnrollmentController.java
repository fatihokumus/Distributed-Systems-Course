package com.campusflow.monolith.enrollment;

import com.campusflow.monolith.events.EnrollmentEventPublisher;
import com.campusflow.monolith.events.EnrollmentRequested;
import com.campusflow.monolith.idempotency.IdempotencyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrollments")
@Validated
public class EnrollmentController {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentController.class);

    private final EnrollmentService enrollmentService;
    private final IdempotencyService idempotencyService;
    private final EnrollmentEventPublisher eventPublisher;
    private final String flowMode;

    public EnrollmentController(EnrollmentService enrollmentService,
                                IdempotencyService idempotencyService,
                                EnrollmentEventPublisher eventPublisher,
                                @Value("${enrollment.flow.mode:async}") String flowMode) {
        this.enrollmentService = enrollmentService;
        this.idempotencyService = idempotencyService;
        this.eventPublisher = eventPublisher;
        this.flowMode = flowMode;
    }

    @PostMapping
public ResponseEntity<?> enroll(
        @Valid @RequestBody EnrollmentRequest request,
        @RequestHeader("X-Request-Id") String requestId,
        @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {

    var existing = idempotencyService.find(requestId);

    if (existing.isPresent()) {
        return ResponseEntity
                .status(existing.get().getResponseStatus())
                .body(existing.get().getResponseBody());
    }

    // ── Async akış: Kafka event yayınla, hemen 202 dön ──
    if ("async".equalsIgnoreCase(flowMode)) {
        log.info("Async enrollment flow: requestId={}, studentNo={}, courseCode={}",
                requestId, request.studentNo(), request.courseCode());

        Enrollment pending = enrollmentService.createPendingEnrollment(request, requestId);

        EnrollmentRequested event = new EnrollmentRequested(
                requestId,
                0L, /* unused — consumer uses requestId to find enrollment */
                0L, /* unused — student lookup is by studentNo */
                0L, /* unused — course lookup is by courseCode */
                request.courseCode(),
                Instant.now()
        );
        eventPublisher.publishRequested(event);

        EnrollmentResponse response = new EnrollmentResponse(
                pending.getId(),
                EnrollmentStatus.PENDING,
                request.studentNo(),
                request.courseCode(),
                "Enrollment request accepted, awaiting capacity confirmation."
        );

        idempotencyService.save(requestId, HttpStatus.ACCEPTED.value(), response.toString());

        return ResponseEntity.accepted().body(response);
    }

    // ── Sync akış: mevcut HTTP/CapacityClient mantığı (değişmedi) ──
    EnrollmentResponse response = enrollmentService.enroll(request, correlationId);

    HttpStatus status = (response.status() == EnrollmentStatus.CONFIRMED)
            ? HttpStatus.CREATED
            : HttpStatus.CONFLICT;

    String responseBody = response.toString();
    idempotencyService.save(requestId, status.value(), responseBody);

    if (response.status() == EnrollmentStatus.CONFIRMED) {
        return ResponseEntity
                .created(URI.create("/api/v1/enrollments/" + response.enrollmentId()))
                .body(response);
    }

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
}

    @GetMapping
    public List<StudentEnrollmentResponse> getStudentEnrollments(
            @RequestParam @NotBlank String studentNo) {
        return enrollmentService.getEnrollmentsByStudentNo(studentNo);
    }
}