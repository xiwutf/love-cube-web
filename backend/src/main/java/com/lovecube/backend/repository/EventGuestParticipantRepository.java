package com.lovecube.backend.repository;

import com.lovecube.backend.entity.EventGuestParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventGuestParticipantRepository extends JpaRepository<EventGuestParticipant, Long> {

    Optional<EventGuestParticipant> findByEventIdAndGuestToken(String eventId, String guestToken);
}
