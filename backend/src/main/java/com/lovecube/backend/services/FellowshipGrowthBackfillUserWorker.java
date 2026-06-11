package com.lovecube.backend.services;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 单用户补偿事务边界：失败不影响其他用户。
 */
@Service
public class FellowshipGrowthBackfillUserWorker {
    private final UserRepository userRepository;
    private final FellowshipProfileGrowthService fellowshipProfileGrowthService;

    public FellowshipGrowthBackfillUserWorker(
            UserRepository userRepository,
            FellowshipProfileGrowthService fellowshipProfileGrowthService
    ) {
        this.userRepository = userRepository;
        this.fellowshipProfileGrowthService = fellowshipProfileGrowthService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FellowshipProfileGrowthService.ProfileSyncStats syncOneUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + userId));
        return fellowshipProfileGrowthService.syncProfileMilestonesWithStats(user);
    }
}
