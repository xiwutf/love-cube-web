package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserDailySwipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface UserDailySwipeRepository extends JpaRepository<UserDailySwipe, Long> {
    Optional<UserDailySwipe> findByUserIdAndSwipeDate(Long userId, LocalDate swipeDate);
}
