package com.campusflow.monolith.course;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<CourseResponse> getCourses() {
        return courseRepository.findAllByOrderByCodeAsc().stream()
                .map(course -> new CourseResponse(
                        course.getCode(),
                        course.getName(),
                        course.getCapacity(),
                        course.getEnrolledCount(),
                        course.getCapacity() - course.getEnrolledCount()
                ))
                .toList();
    }
}
