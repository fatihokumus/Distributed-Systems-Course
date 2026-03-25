package com.campusflow.monolith.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestDeduplicationRepository extends JpaRepository<RequestDeduplication, String> {
}