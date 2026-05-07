package com.lovecube.backend.growth.repository;

import com.lovecube.backend.growth.entity.UserAchievement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    Page<UserAchievement> findByUserIdOrderByGrantedAtDesc(Long userId, Pageable pageable);
}
