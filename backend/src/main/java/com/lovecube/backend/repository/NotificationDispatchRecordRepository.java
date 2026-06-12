package com.lovecube.backend.repository;

import com.lovecube.backend.entity.NotificationDispatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationDispatchRecordRepository extends JpaRepository<NotificationDispatchRecord, Long> {

    List<NotificationDispatchRecord> findByNotificationIdOrderByIdAsc(Long notificationId);
}
