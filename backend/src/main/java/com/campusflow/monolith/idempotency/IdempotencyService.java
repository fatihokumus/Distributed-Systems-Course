package com.campusflow.monolith.idempotency;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IdempotencyService {

    private final RequestDeduplicationRepository repository;

    public IdempotencyService(RequestDeduplicationRepository repository) {
        this.repository = repository;
    }

    public Optional<RequestDeduplication> find(String requestId) {
        return repository.findById(requestId);
    }

    public void save(String requestId, int status, String responseBody) {
        repository.save(new RequestDeduplication(requestId, status, responseBody));
    }
}