package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatformGroupSeason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformGroupSeasonRepository extends JpaRepository<PlatformGroupSeason, Long> {
    Optional<PlatformGroupSeason> findFirstByStatusOrderByStartDateDesc(String status);
}
