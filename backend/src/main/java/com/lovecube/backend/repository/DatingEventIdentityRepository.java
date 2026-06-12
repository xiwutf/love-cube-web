package com.lovecube.backend.repository;

import com.lovecube.backend.entity.DatingEventIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DatingEventIdentityRepository extends JpaRepository<DatingEventIdentity, Long> {

    Optional<DatingEventIdentity> findByEventIdAndUserId(String eventId, Long userId);

    Optional<DatingEventIdentity> findByEventIdAndGuestParticipantId(String eventId, Long guestParticipantId);

    List<DatingEventIdentity> findByEventIdOrderByGenderSideAscBadgeSeqAsc(String eventId);

    @Query("SELECT COALESCE(MAX(i.badgeSeq), 0) FROM DatingEventIdentity i "
            + "WHERE i.eventId = :eventId AND i.genderSide = :genderSide")
    int findMaxBadgeSeq(@Param("eventId") String eventId, @Param("genderSide") String genderSide);
}
