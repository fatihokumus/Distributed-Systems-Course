package com.campusflow.capacity.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Capacity-service tarafından Kafka'ya cevap event'leri yayınlayan publisher.
 * EnrollmentConfirmed veya EnrollmentRejected event'lerini ilgili topic'lere yayınlar.
 * Backend bu event'leri consume ederek enrollment statüsünü günceller.
 */
@Component
public class EnrollmentResponsePublisher {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentResponsePublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String confirmedTopic;
    private final String rejectedTopic;

    public EnrollmentResponsePublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.enrollment-confirmed}") String confirmedTopic,
            @Value("${app.kafka.topics.enrollment-rejected}") String rejectedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.confirmedTopic = confirmedTopic;
        this.rejectedTopic = rejectedTopic;
    }

    public void publishConfirmed(EnrollmentConfirmed event) {
        log.info("Publishing EnrollmentConfirmed: requestId={}, enrollmentId={}, courseId={}, remainingCapacity={}",
                event.requestId(), event.enrollmentId(), event.courseId(), event.remainingCapacity());
        kafkaTemplate.send(confirmedTopic, event.requestId(), event);
    }

    public void publishRejected(EnrollmentRejected event) {
        log.info("Publishing EnrollmentRejected: requestId={}, enrollmentId={}, courseId={}, reason={}",
                event.requestId(), event.enrollmentId(), event.courseId(), event.reason());
        kafkaTemplate.send(rejectedTopic, event.requestId(), event);
    }
}
