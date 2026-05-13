package com.campusflow.monolith.events;

import java.time.Instant;

/**
 * Capacity-service → Backend yönünde gönderilen event.
 * Kapasitenin yeterli olduğunu ve kaydın onaylandığını temsil eder.
 *
 * @param requestId          Orijinal isteği işaretleyen ID
 * @param enrollmentId       Backend'in güncelleyeceği kayıt ID'si
 * @param courseId           Hangi ders
 * @param remainingCapacity  Bu kayıttan sonra dersin kalan kapasitesi (opsiyonel bilgi)
 * @param occurredAt         Event'in yayınlandığı an
 */
public record EnrollmentConfirmed(
        String requestId,
        Long enrollmentId,
        Long courseId,
        Integer remainingCapacity,
        Instant occurredAt
) {}
