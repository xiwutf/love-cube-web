package com.lovecube.backend.services;

import com.lovecube.backend.entity.FellowshipProfile;
import com.lovecube.backend.entity.FellowshipProfileMain;
import com.lovecube.backend.entity.UserProfile;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.FellowshipProfileMainRepository;
import com.lovecube.backend.repository.FellowshipProfileRepository;
import com.lovecube.backend.repository.UserPhotoRepository;
import com.lovecube.backend.repository.UserProfileRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminFellowshipUserInsightService.InsightBatchContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminFellowshipGrowthDashboardService {
    private final UserRepository userRepository;
    private final UserPhotoRepository userPhotoRepository;
    private final FellowshipProfileMainRepository fellowshipProfileMainRepository;
    private final FellowshipProfileRepository fellowshipProfileRepository;
    private final UserProfileRepository userProfileRepository;
    private final VerificationService verificationService;
    private final AdminAuthService adminAuthService;
    private final AdminFellowshipUserInsightService adminFellowshipUserInsightService;

    public AdminFellowshipGrowthDashboardService(
            UserRepository userRepository,
            UserPhotoRepository userPhotoRepository,
            FellowshipProfileMainRepository fellowshipProfileMainRepository,
            FellowshipProfileRepository fellowshipProfileRepository,
            UserProfileRepository userProfileRepository,
            VerificationService verificationService,
            AdminAuthService adminAuthService,
            AdminFellowshipUserInsightService adminFellowshipUserInsightService
    ) {
        this.userRepository = userRepository;
        this.userPhotoRepository = userPhotoRepository;
        this.fellowshipProfileMainRepository = fellowshipProfileMainRepository;
        this.fellowshipProfileRepository = fellowshipProfileRepository;
        this.userProfileRepository = userProfileRepository;
        this.verificationService = verificationService;
        this.adminAuthService = adminAuthService;
        this.adminFellowshipUserInsightService = adminFellowshipUserInsightService;
    }

    public Map<String, Object> buildDashboard(User operator) {
        boolean hiddenSuperAdminOperator = adminAuthService.isHiddenSuperAdmin(operator);
        List<User> visibleUsers = userRepository.findAll().stream()
                .filter(user -> hiddenSuperAdminOperator || !adminAuthService.isHiddenSuperAdmin(user))
                .collect(Collectors.toList());
        List<Long> userIds = visibleUsers.stream()
                .map(User::getUserid)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        Map<Long, Long> uploadedPhotoCountMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (Object[] row : userPhotoRepository.countGroupedByUserIds(userIds)) {
                if (row == null || row.length < 2) {
                    continue;
                }
                Long uid = row[0] instanceof Number ? ((Number) row[0]).longValue() : null;
                Long count = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
                if (uid != null) {
                    uploadedPhotoCountMap.put(uid, count);
                }
            }
        }
        Map<Long, FellowshipProfileMain> mainProfileMap = userIds.isEmpty()
                ? Map.of()
                : fellowshipProfileMainRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(FellowshipProfileMain::getUserId, item -> item, (a, b) -> a));
        Map<Long, FellowshipProfile> legacyProfileMap = userIds.isEmpty()
                ? Map.of()
                : fellowshipProfileRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(FellowshipProfile::getUserId, item -> item, (a, b) -> a));
        Map<Long, UserProfile> userProfileMap = userIds.isEmpty()
                ? Map.of()
                : userProfileRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, item -> item, (a, b) -> a));
        Map<Long, Map<String, Boolean>> verifySummaryMap = verificationService.getBatchSummary(userIds);

        InsightBatchContext insightContext = adminFellowshipUserInsightService.prepareContext(
                visibleUsers,
                uploadedPhotoCountMap,
                verifySummaryMap,
                mainProfileMap,
                legacyProfileMap,
                userProfileMap
        );
        return adminFellowshipUserInsightService.buildDashboard(visibleUsers, insightContext);
    }
}
