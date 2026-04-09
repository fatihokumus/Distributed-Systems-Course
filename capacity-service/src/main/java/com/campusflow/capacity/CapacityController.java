package com.campusflow.capacity;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/capacity")
public class CapacityController {

    private final CapacityService capacityService;

    public CapacityController(CapacityService capacityService) {
        this.capacityService = capacityService;
    }

    @GetMapping
    public ResponseEntity<List<CapacityCourseResponse>> getAllCourses(
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        return ResponseEntity.ok(capacityService.getAllCourses());
    }

    @GetMapping("/{courseCode}")
    public ResponseEntity<Boolean> hasCapacity(
            @PathVariable String courseCode,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        return ResponseEntity.ok(capacityService.hasCapacity(courseCode));
    }

    @PostMapping("/{courseCode}/increase")
    public ResponseEntity<Void> increase(
            @PathVariable String courseCode,
            @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        capacityService.increase(courseCode);
        return ResponseEntity.ok().build();
    }
}