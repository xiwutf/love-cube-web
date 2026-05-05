package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, Long> {

    List<UserNotificationSetting> findByUserId(Long userId);

    Optional<UserNotificationSetting> findByUserIdAndType(Long userId, String type);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserNotificationSetting s SET s.wechatEnabled = false WHERE s.userId = :userId")
    int disableAllWechatForUser(@Param("userId") Long userId);
}
