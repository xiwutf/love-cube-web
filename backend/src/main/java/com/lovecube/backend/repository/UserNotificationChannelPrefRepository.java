package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserNotificationChannelPref;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserNotificationChannelPrefRepository extends JpaRepository<UserNotificationChannelPref, Long> {

    Optional<UserNotificationChannelPref> findByUserId(Long userId);
}
