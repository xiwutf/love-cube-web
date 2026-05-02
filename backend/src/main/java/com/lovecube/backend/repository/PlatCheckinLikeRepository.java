package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatCheckinLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlatCheckinLikeRepository extends JpaRepository<PlatCheckinLike, Long> {

    Optional<PlatCheckinLike> findByCheckinIdAndUserId(Long checkinId, Long userId);

    boolean existsByCheckinIdAndUserId(Long checkinId, Long userId);

    long countByCheckinId(Long checkinId);

    void deleteByCheckinIdAndUserId(Long checkinId, Long userId);

    @Query("SELECT l.checkinId, COUNT(l) FROM PlatCheckinLike l WHERE l.checkinId IN :ids GROUP BY l.checkinId")
    List<Object[]> countByCheckinIdInGrouped(@Param("ids") List<Long> ids);

    @Query("SELECT l.checkinId FROM PlatCheckinLike l WHERE l.userId = :userId AND l.checkinId IN :ids")
    List<Long> findCheckinIdsLikedByUser(@Param("userId") Long userId, @Param("ids") List<Long> ids);
}
