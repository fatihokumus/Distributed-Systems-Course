package com.campusflow.monolith.events;

import java.time.Instant;

/**
 * Capacity-service → Backend yönünde gönderilen event.
 * Kapasitenin yetersiz olduğunu ve kaydın reddedildiğini temsil eder.
 *
 * @param requestId    Orijinal isteği işaretleyen ID
 * @param enrollmentId Backend'in güncelleyeceği kayıt ID'si
 * @param courseId     Hangi ders
 * @param reason       Reddedilme sebebi (örn. CAPACITY_FULL, COURSE_NOT_FOUND)
 * @param occurredAt   Event'in yayınlandığı an
 */
public record EnrollmentRejected(
        String requestId,
        Long enrollmentId,
        Long courseId,
        String reason,
        Instant occurredAt
) {}
