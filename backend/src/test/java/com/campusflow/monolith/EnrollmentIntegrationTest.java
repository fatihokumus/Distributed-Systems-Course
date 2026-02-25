package com.campusflow.monolith;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class EnrollmentIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("campusflow")
            .withUsername("campusflow")
            .withPassword("campusflow");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetData() {
        jdbcTemplate.execute("DELETE FROM audit_entries");
        jdbcTemplate.execute("DELETE FROM enrollments");
        jdbcTemplate.execute("DELETE FROM students");
        jdbcTemplate.execute("UPDATE courses SET enrolled_count = 0");
    }

    @Test
    void enrollingThreeStudentsIntoCse401AllowsOnlyTwoConfirmed() throws Exception {
        List<String> students = List.of("20201001", "20201002", "20201003");
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch ready = new CountDownLatch(3);
        CountDownLatch start = new CountDownLatch(1);

        List<Callable<EnrollmentResponse>> tasks = new ArrayList<>();
        for (String student : students) {
            tasks.add(() -> {
                ready.countDown();
                start.await(5, TimeUnit.SECONDS);
                return postEnrollment(student, "CSE401");
            });
        }

        List<Future<EnrollmentResponse>> futures = tasks.stream().map(executorService::submit).toList();
        ready.await(5, TimeUnit.SECONDS);
        start.countDown();

        List<EnrollmentResponse> responses = new ArrayList<>();
        for (Future<EnrollmentResponse> future : futures) {
            responses.add(future.get(10, TimeUnit.SECONDS));
        }
        executorService.shutdownNow();

        long confirmed = responses.stream().filter(r -> "CONFIRMED".equals(r.status)).count();
        long rejected = responses.stream().filter(r -> "REJECTED".equals(r.status)).count();

        assertThat(confirmed).isEqualTo(2);
        assertThat(rejected).isEqualTo(1);

        Integer enrolledCount = jdbcTemplate.queryForObject(
                "SELECT enrolled_count FROM courses WHERE code = 'CSE401'",
                Integer.class
        );
        assertThat(enrolledCount).isEqualTo(2);
    }

    @Test
    void auditEntriesAreWrittenForEachEnrollmentAttempt() {
        postEnrollment("20202001", "CSE403");
        postEnrollment("20202002", "CSE403");
        postEnrollment("20202003", "CSE403");

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM audit_entries", Integer.class);
        assertThat(count).isEqualTo(3);

        AuditEntryResponse[] entries = restTemplate.getForObject(
                url("/api/v1/audit?limit=50"),
                AuditEntryResponse[].class
        );

        assertThat(entries).isNotNull();
        assertThat(entries.length).isGreaterThanOrEqualTo(3);
    }

    private EnrollmentResponse postEnrollment(String studentNo, String courseCode) {
        var response = restTemplate.postForEntity(
                url("/api/v1/enrollments"),
                Map.of("studentNo", studentNo, "courseCode", courseCode),
                EnrollmentResponse.class
        );

        assertThat(response.getStatusCode()).isIn(CREATED, CONFLICT);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    static class EnrollmentResponse {
        public String status;
    }

    static class AuditEntryResponse {
        public String eventType;
    }
}
