package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserAccountTaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountTaskProgressRepository extends JpaRepository<UserAccountTaskProgress, Long> {
    Optional<UserAccountTaskProgress> findByUserIdAndTaskCode(Long userId, String taskCode);

    List<UserAccountTaskProgress> findByUserId(Long userId);
}
