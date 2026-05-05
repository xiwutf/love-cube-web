package com.lovecube.backend.repository;

import com.lovecube.backend.entity.EventSignup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventSignupRepository extends JpaRepository<EventSignup, Long> {
    boolean existsByEventIdAndUserId(String eventId, Long userId);

    long countByEventId(String eventId);

    long countByUserId(Long userId);

    List<EventSignup> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);
}
