package com.lovecube.backend.repository;

import com.lovecube.backend.entity.ActivityReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityReminderLogRepository extends JpaRepository<ActivityReminderLog, Long> {

    boolean existsByActivitySourceAndActivityIdAndUserIdAndReminderType(
            String activitySource, String activityId, Long userId, String reminderType);
}
