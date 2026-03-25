package com.campusflow.monolith.capacity;

import com.campusflow.monolith.course.Course;
import com.campusflow.monolith.course.CourseRepository;
import com.campusflow.monolith.common.CourseNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CapacityServiceImpl implements CapacityService {

    private final CourseRepository courseRepository;

    public CapacityServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public boolean hasCapacity(String courseCode) {
        Course course = courseRepository.findByCodeForUpdate(courseCode)
                .orElseThrow(() -> new CourseNotFoundException(courseCode));

        return course.getEnrolledCount() < course.getCapacity();
    }

    @Override
    public void increaseEnrollment(String courseCode) {
        Course course = courseRepository.findByCodeForUpdate(courseCode)
                .orElseThrow(() -> new CourseNotFoundException(courseCode));

        course.setEnrolledCount(course.getEnrolledCount() + 1);
        courseRepository.save(course);
    }
}