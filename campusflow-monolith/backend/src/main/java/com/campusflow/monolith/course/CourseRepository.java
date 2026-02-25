package com.campusflow.monolith.course;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    Optional<Course> findByCode(String code);

    List<Course> findAllByOrderByCodeAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Course c where c.code = :code")
    Optional<Course> findByCodeForUpdate(@Param("code") String code);
}
