package com.lovecube.backend.config;

import com.lovecube.backend.services.SpaceCampaignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SpaceCampaignScheduler {

    private static final Logger log = LoggerFactory.getLogger(SpaceCampaignScheduler.class);

    private final SpaceCampaignService spaceCampaignService;

    public SpaceCampaignScheduler(SpaceCampaignService spaceCampaignService) {
        this.spaceCampaignService = spaceCampaignService;
    }

    /** 每天 00:05 刷新 scheduled / active / ended */
    @Scheduled(cron = "0 5 0 * * ?")
    public void refreshCampaignStatuses() {
        try {
            spaceCampaignService.refreshAllCampaignStatusesScheduled();
        } catch (Exception e) {
            log.warn("打卡营状态定时刷新失败: {}", e.getMessage());
        }
    }

    /** 每天 09:00 提醒当日未完成成员 */
    @Scheduled(cron = "0 0 9 * * ?")
    public void morningReminders() {
        try {
            spaceCampaignService.sendScheduledReminders(false);
        } catch (Exception e) {
            log.warn("打卡营早间提醒失败: {}", e.getMessage());
        }
    }

    /** 每天 21:00 再次提醒当日未完成成员 */
    @Scheduled(cron = "0 0 21 * * ?")
    public void eveningReminders() {
        try {
            spaceCampaignService.sendScheduledReminders(true);
        } catch (Exception e) {
            log.warn("打卡营晚间提醒失败: {}", e.getMessage());
        }
    }
}
