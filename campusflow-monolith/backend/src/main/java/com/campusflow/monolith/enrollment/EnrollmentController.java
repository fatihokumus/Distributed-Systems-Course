package com.campusflow.monolith.enrollment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.enroll(request);

        if (response.status() == EnrollmentStatus.CONFIRMED) {
            return ResponseEntity
                    .created(URI.create("/api/v1/enrollments/" + response.enrollmentId()))
                    .body(response);
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @GetMapping
    public List<StudentEnrollmentResponse> getStudentEnrollments(@RequestParam @NotBlank String studentNo) {
        return enrollmentService.getEnrollmentsByStudentNo(studentNo);
    }
}
