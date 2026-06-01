package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserNewcomerTaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserNewcomerTaskProgressRepository extends JpaRepository<UserNewcomerTaskProgress, Long> {
    Optional<UserNewcomerTaskProgress> findByUserIdAndTaskCode(Long userId, String taskCode);
}
