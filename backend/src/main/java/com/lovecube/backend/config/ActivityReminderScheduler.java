package com.lovecube.backend.config;

import com.lovecube.backend.services.ActivityReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ActivityReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ActivityReminderScheduler.class);

    private final ActivityReminderService reminderService;

    public ActivityReminderScheduler(ActivityReminderService reminderService) {
        this.reminderService = reminderService;
    }

    /** 每 15 分钟扫描一次活动提醒窗口 */
    @Scheduled(cron = "0 */15 * * * ?")
    public void dispatchActivityReminders() {
        try {
            reminderService.runAllReminders();
        } catch (Exception e) {
            log.warn("活动提醒定时任务失败: {}", e.getMessage(), e);
        }
    }
}
