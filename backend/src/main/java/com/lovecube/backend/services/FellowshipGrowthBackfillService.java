package com.lovecube.backend.services;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 历史用户成长/徽章补偿：复用 dedupeKey，不重复发放经验。
 */
@Service
public class FellowshipGrowthBackfillService {
    private static final Logger log = LoggerFactory.getLogger(FellowshipGrowthBackfillService.class);

    private final UserRepository userRepository;
    private final FellowshipGrowthBackfillUserWorker backfillUserWorker;

    public FellowshipGrowthBackfillService(
            UserRepository userRepository,
            FellowshipGrowthBackfillUserWorker backfillUserWorker
    ) {
        this.userRepository = userRepository;
        this.backfillUserWorker = backfillUserWorker;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Map<String, Object> syncExistingUsers() {
        List<Long> userIds = userRepository.findAll().stream()
                .filter(this::isActiveUser)
                .map(User::getUserid)
                .toList();

        int scannedUsers = 0;
        int growthEventsCreated = 0;
        int badgesUnlocked = 0;
        int skippedUsers = 0;

        for (Long userId : userIds) {
            scannedUsers++;
            try {
                FellowshipProfileGrowthService.ProfileSyncStats stats = backfillUserWorker.syncOneUser(userId);
                growthEventsCreated += stats.growthEventsCreated();
                badgesUnlocked += stats.badgesUnlocked();
                if (stats.isSkipped()) {
                    skippedUsers++;
                }
            } catch (Exception ex) {
                skippedUsers++;
                log.warn("Fellowship growth backfill skipped userId={}: {}", userId, ex.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scannedUsers", scannedUsers);
        result.put("growthEventsCreated", growthEventsCreated);
        result.put("badgesUnlocked", badgesUnlocked);
        result.put("skippedUsers", skippedUsers);

        log.info(
                "Fellowship growth backfill finished: scannedUsers={}, growthEventsCreated={}, badgesUnlocked={}, skippedUsers={}",
                scannedUsers,
                growthEventsCreated,
                badgesUnlocked,
                skippedUsers
        );
        return result;
    }

    private boolean isActiveUser(User user) {
        if (user == null || user.getUserid() == null) {
            return false;
        }
        String status = user.getUserStatus();
        return status == null || !"DISABLED".equalsIgnoreCase(status.trim());
    }
}
