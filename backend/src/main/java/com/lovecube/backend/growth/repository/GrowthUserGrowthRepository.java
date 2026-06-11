package com.lovecube.backend.growth.repository;

import com.lovecube.backend.growth.entity.UserGrowth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GrowthUserGrowthRepository extends JpaRepository<UserGrowth, Long> {
    Optional<UserGrowth> findByUserId(Long userId);

    List<UserGrowth> findByUserIdIn(Iterable<Long> userIds);

    long countByTotalContributionGreaterThan(int totalContribution);

    @Query("SELECT g.level, COUNT(g) FROM GrowthUserGrowth g GROUP BY g.level ORDER BY g.level ASC")
    List<Object[]> countLevelDistribution();
}
