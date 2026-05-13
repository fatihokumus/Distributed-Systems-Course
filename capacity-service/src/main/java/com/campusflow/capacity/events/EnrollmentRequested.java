package com.campusflow.capacity.events;

import java.time.Instant;

/**
 * Backend → Capacity-service yönünde gelen event.
 * Capacity-service bu event'i CONSUME eder.
 *
 * @param requestId    Idempotency anahtarı
 * @param enrollmentId Backend'in DB'sindeki kayıt ID'si
 * @param studentId    Hangi öğrenci
 * @param courseId     Hangi ders
 * @param courseCode   Dersin kodu (capacity-service course lookup için)
 * @param occurredAt   Event'in yayınlandığı an
 */
public record EnrollmentRequested(
        String requestId,
        Long enrollmentId,
        Long studentId,
        Long courseId,
        String courseCode,
        Instant occurredAt
) {}
