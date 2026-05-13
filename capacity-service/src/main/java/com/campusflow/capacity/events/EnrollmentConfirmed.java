package com.campusflow.capacity.events;

import java.time.Instant;

/**
 * Capacity-service → Backend yönünde gönderilen event.
 * Capacity-service bu event'i PUBLISH eder (kapasite yeterliyse).
 *
 * @param requestId          Orijinal isteğin ID'si
 * @param enrollmentId       Backend'in güncelleyeceği kayıt ID'si
 * @param courseId           Hangi ders
 * @param remainingCapacity  Bu kayıttan sonra dersin kalan kapasitesi
 * @param occurredAt         Event'in yayınlandığı an
 */
public record EnrollmentConfirmed(
        String requestId,
        Long enrollmentId,
        Long courseId,
        Integer remainingCapacity,
        Instant occurredAt
) {}
