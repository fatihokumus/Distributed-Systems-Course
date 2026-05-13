package com.campusflow.capacity.events;

import com.campusflow.capacity.Course;
import com.campusflow.capacity.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Backend'den gelen EnrollmentRequested event'lerini dinler.
 * courseCode ile course'u bulur, kapasite kontrolü yapar:
 * - Kapasite varsa: enrolledCount artırır, EnrollmentConfirmed yayınlar
 * - Kapasite yoksa: EnrollmentRejected yayınlar (reason=CAPACITY_FULL)
 * - Course bulunamazsa: EnrollmentRejected yayınlar (reason=COURSE_NOT_FOUND)
 */
@Component
public class EnrollmentRequestConsumer {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentRequestConsumer.class);

    private final CourseRepository courseRepository;
    private final EnrollmentResponsePublisher responsePublisher;

    public EnrollmentRequestConsumer(CourseRepository courseRepository,
                                     EnrollmentResponsePublisher responsePublisher) {
        this.courseRepository = courseRepository;
        this.responsePublisher = responsePublisher;
    }

    @Transactional
    @KafkaListener(
        topics = "${app.kafka.topics.enrollment-requested}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onEnrollmentRequested(@Payload EnrollmentRequested event,
                                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Received EnrollmentRequested from topic={}: requestId={}, courseCode={}",
                topic, event.requestId(), event.courseCode());

        if (event.courseCode() == null || event.courseCode().isBlank()) {
            log.warn("EnrollmentRequested has no courseCode, rejecting requestId={}", event.requestId());
            responsePublisher.publishRejected(new EnrollmentRejected(
                    event.requestId(),
                    event.enrollmentId(),
                    event.courseId(),
                    "COURSE_CODE_MISSING",
                    Instant.now()
            ));
            return;
        }

        Optional<Course> courseOpt = courseRepository.findById(event.courseCode());

        if (courseOpt.isEmpty()) {
            log.warn("Course not found for courseCode={}, rejecting enrollment requestId={}",
                    event.courseCode(), event.requestId());

            responsePublisher.publishRejected(new EnrollmentRejected(
                    event.requestId(),
                    event.enrollmentId(),
                    event.courseId(),
                    "COURSE_NOT_FOUND",
                    Instant.now()
            ));
            return;
        }

        Course course = courseOpt.get();

        if (course.getEnrolledCount() >= course.getCapacity()) {
            log.info("Capacity full for courseCode={}, rejecting enrollment requestId={}",
                    event.courseCode(), event.requestId());

            responsePublisher.publishRejected(new EnrollmentRejected(
                    event.requestId(),
                    event.enrollmentId(),
                    event.courseId(),
                    "CAPACITY_FULL",
                    Instant.now()
            ));
            return;
        }

        // Kapasite var — enrolled count artır ve confirm et
        course.setEnrolledCount(course.getEnrolledCount() + 1);
        courseRepository.save(course);

        int remainingCapacity = course.getCapacity() - course.getEnrolledCount();

        log.info("Capacity reserved for courseCode={}, remainingCapacity={}, confirming requestId={}",
                event.courseCode(), remainingCapacity, event.requestId());

        responsePublisher.publishConfirmed(new EnrollmentConfirmed(
                event.requestId(),
                event.enrollmentId(),
                event.courseId(),
                remainingCapacity,
                Instant.now()
        ));
    }
}
