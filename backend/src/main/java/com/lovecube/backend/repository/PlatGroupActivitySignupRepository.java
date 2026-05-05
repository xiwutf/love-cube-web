package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatGroupActivitySignup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlatGroupActivitySignupRepository extends JpaRepository<PlatGroupActivitySignup, Long> {

    Optional<PlatGroupActivitySignup> findByActivityIdAndUserId(Long activityId, Long userId);

    int countByActivityIdAndStatus(Long activityId, String status);

    List<PlatGroupActivitySignup> findByActivityId(Long activityId);

    List<PlatGroupActivitySignup> findByActivityIdInAndUserIdAndStatus(
            List<Long> activityIds, Long userId, String status);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);
}
