package com.lovecube.backend.config;

import com.lovecube.backend.services.DatingMutualMatchJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DatingMutualMatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(DatingMutualMatchScheduler.class);

    private final DatingMutualMatchJobService jobService;

    public DatingMutualMatchScheduler(DatingMutualMatchJobService jobService) {
        this.jobService = jobService;
    }

    /** 每 5 分钟扫描已结束的联谊专场，在活动结束后 5~30 分钟窗口内自动计算互选 */
    @Scheduled(cron = "0 */5 * * * ?")
    public void autoRecomputeMutualMatches() {
        try {
            int processed = jobService.runAutoRecomputeAfterEnd();
            if (processed > 0) {
                log.info("联谊自动互选定时任务完成，处理活动数={}", processed);
            }
        } catch (Exception e) {
            log.warn("联谊自动互选定时任务失败: {}", e.getMessage(), e);
        }
    }
}
