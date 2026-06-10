package com.lovecube.backend.config;

import com.lovecube.backend.services.GroupWeeklyDigestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class GroupWeeklyDigestScheduler {

    private static final Logger log = LoggerFactory.getLogger(GroupWeeklyDigestScheduler.class);

    private final GroupWeeklyDigestService digestService;

    public GroupWeeklyDigestScheduler(GroupWeeklyDigestService digestService) {
        this.digestService = digestService;
    }

    /** 每周一 09:00 向各团体管理员发送周报（同一团体每周仅一次） */
    @Scheduled(cron = "0 0 9 ? * MON")
    public void sendWeeklyDigests() {
        try {
            int count = digestService.sendAllWeeklyDigests();
            log.info("团体周报已发送 {} 个团体", count);
        } catch (Exception e) {
            log.warn("团体周报定时任务失败: {}", e.getMessage());
        }
    }
}
