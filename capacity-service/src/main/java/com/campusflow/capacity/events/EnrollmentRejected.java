package com.campusflow.capacity.events;

import java.time.Instant;

/**
 * Capacity-service → Backend yönünde gönderilen event.
 * Capacity-service bu event'i PUBLISH eder (kapasite yetersizse veya hata varsa).
 *
 * @param requestId    Orijinal isteğin ID'si
 * @param enrollmentId Backend'in güncelleyeceği kayıt ID'si
 * @param courseId     Hangi ders
 * @param reason       Reddedilme sebebi (CAPACITY_FULL, COURSE_NOT_FOUND, vb.)
 * @param occurredAt   Event'in yayınlandığı an
 */
public record EnrollmentRejected(
        String requestId,
        Long enrollmentId,
        Long courseId,
        String reason,
        Instant occurredAt
) {}
