package com.campusflow.capacity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CapacityService {

    private final CourseRepository courseRepository;

    public CapacityService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public boolean hasCapacity(String courseCode) {
        Course course = courseRepository.findById(courseCode)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseCode));
        return course.getEnrolledCount() < course.getCapacity();
    }

    @Transactional
    public void increase(String courseCode) {
        Course course = courseRepository.findById(courseCode)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseCode));

        if (course.getEnrolledCount() >= course.getCapacity()) {
            throw new RuntimeException("No remaining capacity for course: " + courseCode);
        }

        course.setEnrolledCount(course.getEnrolledCount() + 1);
        courseRepository.save(course);
    }

    public List<CapacityCourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(course -> new CapacityCourseResponse(
                        course.getCourseCode(),
                        course.getCapacity(),
                        course.getEnrolledCount()
                ))
                .toList();
    }
}