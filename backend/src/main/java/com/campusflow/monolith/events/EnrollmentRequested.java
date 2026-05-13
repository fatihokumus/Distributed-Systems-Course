package com.campusflow.monolith.events;

import java.time.Instant;

/**
 * Backend → Capacity-service yönünde gönderilen event.
 * Bir öğrencinin bir derse kayıt olma isteğini temsil eder.
 *
 * @param requestId    Idempotency anahtarı (aynı requestId iki kez gelirse capacity tek işlem yapar)
 * @param enrollmentId Backend'in DB'sinde oluşturduğu kayıt ID'si (cevap event'inde geri gelir)
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
