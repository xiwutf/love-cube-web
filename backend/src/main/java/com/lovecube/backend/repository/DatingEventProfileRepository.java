package com.lovecube.backend.repository;

import com.lovecube.backend.entity.DatingEventProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DatingEventProfileRepository extends JpaRepository<DatingEventProfile, Long> {

    Optional<DatingEventProfile> findByEventIdAndUserId(String eventId, Long userId);

    Optional<DatingEventProfile> findByEventIdAndGuestParticipantId(String eventId, Long guestParticipantId);

    List<DatingEventProfile> findByEventIdAndUserIdIn(String eventId, List<Long> userIds);

    List<DatingEventProfile> findByEventIdAndGuestParticipantIdIn(String eventId, List<Long> guestParticipantIds);
}
