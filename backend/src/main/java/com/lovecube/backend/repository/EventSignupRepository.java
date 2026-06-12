package com.lovecube.backend.repository;

import com.lovecube.backend.entity.EventSignup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventSignupRepository extends JpaRepository<EventSignup, Long> {
    boolean existsByEventIdAndUserId(String eventId, Long userId);

    boolean existsByEventIdAndGuestParticipantId(String eventId, Long guestParticipantId);

    Optional<EventSignup> findByEventIdAndUserId(String eventId, Long userId);

    Optional<EventSignup> findByEventIdAndGuestParticipantId(String eventId, Long guestParticipantId);

    List<EventSignup> findByEventIdAndCheckedInTrue(String eventId);

    long countByEventIdAndCheckedInTrue(String eventId);

    long countByEventId(String eventId);

    long countByUserId(Long userId);

    List<EventSignup> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<EventSignup> findByEventId(String eventId);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);
}
