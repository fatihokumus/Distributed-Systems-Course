package com.campusflow.monolith.capacityclient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class CapacityClient {

    private static final Logger log = LoggerFactory.getLogger(CapacityClient.class);
    private static final String CB_NAME = "capacityService";

    private final RestTemplate restTemplate;
    private final String capacityBaseUrl;

    public CapacityClient(RestTemplate restTemplate,
                          @Value("${capacity.base-url}") String capacityBaseUrl) {
        this.restTemplate = restTemplate;
        this.capacityBaseUrl = capacityBaseUrl;
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "hasCapacityFallback")
    @Retryable(
            retryFor = {RestClientException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0, random = true)
    )
    public boolean hasCapacity(String courseCode, String correlationId) {
        int attempt = RetrySynchronizationManager.getContext() != null
                ? RetrySynchronizationManager.getContext().getRetryCount() + 1
                : 1;

        log.info("[ID: {}] Attempt {}/3 - Checking capacity for courseCode={}",
                correlationId, attempt, courseCode);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-Id", correlationId);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = capacityBaseUrl + "/api/v1/capacity/" + courseCode;

        ResponseEntity<Boolean> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Boolean.class
        );

        return Boolean.TRUE.equals(response.getBody());
    }

    @CircuitBreaker(name = CB_NAME, fallbackMethod = "increaseFallback")
    @Retryable(
            retryFor = {RestClientException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0, random = true)
    )
    public void increase(String courseCode, String correlationId) {
        int attempt = RetrySynchronizationManager.getContext() != null
                ? RetrySynchronizationManager.getContext().getRetryCount() + 1
                : 1;

        log.info("[ID: {}] Attempt {}/3 - Increasing capacity usage for courseCode={}",
                correlationId, attempt, courseCode);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Correlation-Id", correlationId);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = capacityBaseUrl + "/api/v1/capacity/" + courseCode + "/increase";

        restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Void.class
        );
    }

    public boolean hasCapacityFallback(String courseCode,
                                       String correlationId,
                                       CallNotPermittedException e) {
        log.warn("[ID: {}] Circuit breaker OPEN for courseCode={}. Fallback returning false.",
                correlationId, courseCode);
        return false;
    }

    public boolean hasCapacityFallback(String courseCode,
                                       String correlationId,
                                       Exception e) {
        log.warn("[ID: {}] Capacity unavailable for courseCode={}. Fallback returning false. Cause={}",
                correlationId, courseCode, e.toString());
        return false;
    }

    public void increaseFallback(String courseCode,
                                 String correlationId,
                                 CallNotPermittedException e) {
        log.warn("[ID: {}] Circuit breaker OPEN on increase for courseCode={}.",
                correlationId, courseCode);
        throw new CapacityUnavailableException("Capacity service circuit breaker is open", e);
    }

    public void increaseFallback(String courseCode,
                                 String correlationId,
                                 Exception e) {
        log.warn("[ID: {}] Capacity unavailable on increase for courseCode={}. Cause={}",
                correlationId, courseCode, e.toString());
        throw new CapacityUnavailableException("Capacity service unavailable during increase", e);
    }
}