package com.lovecube.backend.config;

import com.lovecube.backend.services.DragonBoat2026CampaignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 端午活动定时任务：启动时与每天 00:10 检查是否发布破冰/收官帖、下线话题。
 */
@Component
public class DragonBoat2026CampaignScheduler {

    private static final Logger log = LoggerFactory.getLogger(DragonBoat2026CampaignScheduler.class);

    private final DragonBoat2026CampaignService campaignService;

    public DragonBoat2026CampaignScheduler(DragonBoat2026CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        runSafely("startup");
    }

    /** 每天 00:10 检查（6/16 发破冰帖，6/21 发收官帖，6/22 下线话题） */
    @Scheduled(cron = "0 10 0 * * ?")
    public void dailyCheck() {
        runSafely("cron");
    }

    private void runSafely(String trigger) {
        try {
            campaignService.runScheduledTasks();
        } catch (Exception ex) {
            log.warn("端午活动定时任务失败 trigger={}: {}", trigger, ex.getMessage());
        }
    }
}
