package com.lovecube.backend.repository;

import com.lovecube.backend.entity.EventSignup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventSignupRepository extends JpaRepository<EventSignup, Long> {
    boolean existsByEventIdAndUserId(String eventId, Long userId);

    long countByEventId(String eventId);
}
