package com.campusflow.monolith.enrollment;

import com.campusflow.monolith.idempotency.IdempotencyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
@Validated
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final IdempotencyService idempotencyService;

    public EnrollmentController(EnrollmentService enrollmentService,
                                IdempotencyService idempotencyService) {
        this.enrollmentService = enrollmentService;
        this.idempotencyService = idempotencyService;
    }

    @PostMapping
    public ResponseEntity<?> enroll(
            @Valid @RequestBody EnrollmentRequest request,
            @RequestHeader("X-Request-Id") String requestId) {

        var existing = idempotencyService.find(requestId);

        if (existing.isPresent()) {
            return ResponseEntity
                    .status(existing.get().getResponseStatus())
                    .body(existing.get().getResponseBody());
        }

        EnrollmentResponse response = enrollmentService.enroll(request);

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