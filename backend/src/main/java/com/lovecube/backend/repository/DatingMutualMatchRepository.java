package com.lovecube.backend.repository;

import com.lovecube.backend.entity.DatingMutualMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DatingMutualMatchRepository extends JpaRepository<DatingMutualMatch, Long> {

    Optional<DatingMutualMatch> findByEventIdAndParticipantATypeAndParticipantAIdAndParticipantBTypeAndParticipantBId(
            String eventId,
            String participantAType,
            Long participantAId,
            String participantBType,
            Long participantBId
    );

    List<DatingMutualMatch> findByEventIdAndParticipantATypeAndParticipantAId(
            String eventId,
            String participantAType,
            Long participantAId
    );

    List<DatingMutualMatch> findByEventIdAndParticipantBTypeAndParticipantBId(
            String eventId,
            String participantBType,
            Long participantBId
    );

    long countByEventId(String eventId);
}
