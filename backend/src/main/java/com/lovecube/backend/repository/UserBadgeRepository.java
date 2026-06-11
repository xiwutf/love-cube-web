package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUserIdOrderByCreatedAtAsc(Long userId);

    Optional<UserBadge> findByUserIdAndBadgeCode(Long userId, String badgeCode);

    List<UserBadge> findByUserIdInAndUnlocked(Iterable<Long> userIds, int unlocked);
}
