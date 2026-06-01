package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserLoginStreak;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLoginStreakRepository extends JpaRepository<UserLoginStreak, Long> {
}
