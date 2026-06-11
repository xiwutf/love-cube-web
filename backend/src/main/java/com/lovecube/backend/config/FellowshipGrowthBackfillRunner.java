package com.lovecube.backend.config;

import com.lovecube.backend.services.FellowshipGrowthBackfillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 可选启动补偿：lovecube.fellowship.growth-backfill.on-startup=true 时执行一次。
 */
@Component
@ConditionalOnProperty(prefix = "lovecube.fellowship.growth-backfill", name = "on-startup", havingValue = "true")
public class FellowshipGrowthBackfillRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(FellowshipGrowthBackfillRunner.class);

    private final FellowshipGrowthBackfillService fellowshipGrowthBackfillService;

    public FellowshipGrowthBackfillRunner(FellowshipGrowthBackfillService fellowshipGrowthBackfillService) {
        this.fellowshipGrowthBackfillService = fellowshipGrowthBackfillService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Fellowship growth backfill on startup enabled, starting...");
        Map<String, Object> result = fellowshipGrowthBackfillService.syncExistingUsers();
        log.info("Fellowship growth backfill on startup completed: {}", result);
    }
}
