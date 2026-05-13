package com.campusflow.monolith.events;

import com.campusflow.monolith.enrollment.EnrollmentRepository;
import com.campusflow.monolith.enrollment.EnrollmentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Capacity-service'den gelen cevap event'lerini dinler.
 * EnrollmentConfirmed gelirse DB'de status=CONFIRMED,
 * EnrollmentRejected gelirse DB'de status=REJECTED yapar.
 *
 * Kayıt requestId ile bulunur (enrollmentId tipi Long olduğu için
 * UUID-tabanlı entity ile doğrudan eşleşmez; requestId kullanılır).
 */
@Component
public class EnrollmentResponseConsumer {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentResponseConsumer.class);

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentResponseConsumer(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional
    @KafkaListener(
        topics = "${app.kafka.topics.enrollment-confirmed}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onConfirmed(@Payload EnrollmentConfirmed event,
                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Received EnrollmentConfirmed from topic={}: requestId={}, enrollmentId={}, remainingCapacity={}",
                topic, event.requestId(), event.enrollmentId(), event.remainingCapacity());

        enrollmentRepository.findByRequestId(event.requestId())
                .ifPresentOrElse(
                        enrollment -> {
                            enrollment.setStatus(EnrollmentStatus.CONFIRMED);
                            enrollmentRepository.save(enrollment);
                            log.info("Enrollment status updated to CONFIRMED for requestId={}", event.requestId());
                        },
                        () -> log.warn("Enrollment not found for requestId={}, ignoring confirmed event", event.requestId())
                );
    }

    @Transactional
    @KafkaListener(
        topics = "${app.kafka.topics.enrollment-rejected}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onRejected(@Payload EnrollmentRejected event,
                           @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Received EnrollmentRejected from topic={}: requestId={}, enrollmentId={}, reason={}",
                topic, event.requestId(), event.enrollmentId(), event.reason());

        enrollmentRepository.findByRequestId(event.requestId())
                .ifPresentOrElse(
                        enrollment -> {
                            enrollment.setStatus(EnrollmentStatus.REJECTED);
                            enrollmentRepository.save(enrollment);
                            log.info("Enrollment status updated to REJECTED for requestId={}, reason={}",
                                    event.requestId(), event.reason());
                        },
                        () -> log.warn("Enrollment not found for requestId={}, ignoring rejected event", event.requestId())
                );
    }
}
