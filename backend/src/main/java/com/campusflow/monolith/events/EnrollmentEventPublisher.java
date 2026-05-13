package com.campusflow.monolith.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Backend tarafından Kafka'ya event yayınlayan publisher.
 * EnrollmentRequested event'lerini "enrollment-requested" topic'ine yayınlar.
 * Cevap event'leri (Confirmed/Rejected) capacity-service tarafından yayınlanır.
 */
@Component
public class EnrollmentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String enrollmentRequestedTopic;

    public EnrollmentEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.enrollment-requested}") String enrollmentRequestedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.enrollmentRequestedTopic = enrollmentRequestedTopic;
    }

    /**
     * EnrollmentRequested event'ini yayınlar.
     * Key olarak requestId kullanırız: aynı requestId'li mesajlar aynı partition'a düşer,
     * bu sayede sıralama korunur ve idempotency kolaylaşır.
     */
    public void publishRequested(EnrollmentRequested event) {
        log.info("Publishing EnrollmentRequested: requestId={}, enrollmentId={}, studentId={}, courseId={}",
                event.requestId(), event.enrollmentId(), event.studentId(), event.courseId());
        kafkaTemplate.send(enrollmentRequestedTopic, event.requestId(), event);
    }
}
