package com.lovecube.backend.repository;

import com.lovecube.backend.entity.DatingLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DatingLikeRepository extends JpaRepository<DatingLike, Long> {

    Optional<DatingLike> findByEventIdAndParticipantTypeAndParticipantIdAndTargetParticipantTypeAndTargetParticipantId(
            String eventId,
            String participantType,
            Long participantId,
            String targetParticipantType,
            Long targetParticipantId
    );

    List<DatingLike> findByEventIdAndParticipantTypeAndParticipantIdOrderByCreatedAtDesc(
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

    Optional<DatingLike> findByIdAndEventIdAndParticipantTypeAndParticipantId(
            Long id,
            String eventId,
            String participantType,
            Long participantId
    );

    List<DatingLike> findByEventId(String eventId);
}
