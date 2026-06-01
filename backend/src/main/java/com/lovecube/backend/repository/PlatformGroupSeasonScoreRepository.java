package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatformGroupSeasonScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformGroupSeasonScoreRepository extends JpaRepository<PlatformGroupSeasonScore, Long> {
    Optional<PlatformGroupSeasonScore> findBySeasonIdAndGroupId(Long seasonId, Long groupId);

    Page<PlatformGroupSeasonScore> findBySeasonIdOrderByScoreDesc(Long seasonId, Pageable pageable);
}
