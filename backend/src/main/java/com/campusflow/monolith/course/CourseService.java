package com.campusflow.monolith.course;

import com.campusflow.monolith.capacityclient.CapacityCourseResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final RestTemplate restTemplate;
    private final String capacityBaseUrl;

    public CourseService(CourseRepository courseRepository,
                         RestTemplate restTemplate,
                         @Value("${capacity.base-url}") String capacityBaseUrl) {
        this.courseRepository = courseRepository;
        this.restTemplate = restTemplate;
        this.capacityBaseUrl = capacityBaseUrl;
    }

    public List<CourseResponse> getCourses() {
        Map<String, CapacityCourseResponse> capacityMap = fetchCapacityMap();

        return courseRepository.findAllByOrderByCodeAsc().stream()
                .map(course -> {
                    CapacityCourseResponse capacityData = capacityMap.get(course.getCode());

                    int capacity = capacityData != null ? capacityData.capacity() : course.getCapacity();
                    int enrolledCount = capacityData != null ? capacityData.enrolledCount() : course.getEnrolledCount();

                    return new CourseResponse(
                            course.getCode(),
                            course.getName(),
                            capacity,
                            enrolledCount,
                            capacity - enrolledCount
                    );
                })
                .toList();
    }

    private Map<String, CapacityCourseResponse> fetchCapacityMap() {
        String url = capacityBaseUrl + "/api/v1/capacity";

        try {
            ResponseEntity<List<CapacityCourseResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CapacityCourseResponse>>() {}
            );

            List<CapacityCourseResponse> body = response.getBody();
            if (body == null) {
                return Collections.emptyMap();
            }

            return body.stream()
                    .collect(Collectors.toMap(
                            CapacityCourseResponse::courseCode,
                            Function.identity()
                    ));
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}