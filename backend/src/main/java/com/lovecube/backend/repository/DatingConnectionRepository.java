package com.lovecube.backend.repository;

import com.lovecube.backend.entity.DatingConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DatingConnectionRepository extends JpaRepository<DatingConnection, Long> {

    Optional<DatingConnection> findByEventIdAndParticipantTypeAndParticipantIdAndTargetParticipantTypeAndTargetParticipantId(
            String eventId,
            String participantType,
            Long participantId,
            String targetParticipantType,
            Long targetParticipantId
    );

    List<DatingConnection> findByEventIdAndParticipantTypeAndParticipantIdOrderByCreatedAtDesc(
            String eventId,
            String participantType,
            Long participantId
    );

    long countByEventIdAndParticipantTypeAndParticipantId(
            String eventId,
            String participantType,
            Long participantId
    );

    long countByEventId(String eventId);

    Optional<DatingConnection> findByIdAndEventIdAndParticipantTypeAndParticipantId(
            Long id,
            String eventId,
            String participantType,
            Long participantId
    );
}
