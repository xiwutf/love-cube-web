package com.lovecube.backend.services;

import com.lovecube.backend.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminPlatformGroupStatsService {

    private final PlatGroupRepository groupRepository;
    private final PlatGroupMemberRepository memberRepository;
    private final PlatGroupPostRepository postRepository;
    private final PlatGroupCheckinRepository checkinRepository;
    private final PlatGroupActivityRepository activityRepository;
    private final PlatGroupActivitySignupRepository activitySignupRepository;
    private final PlatformGroupSeasonScoreRepository seasonScoreRepository;

    public AdminPlatformGroupStatsService(
            PlatGroupRepository groupRepository,
            PlatGroupMemberRepository memberRepository,
            PlatGroupPostRepository postRepository,
            PlatGroupCheckinRepository checkinRepository,
            PlatGroupActivityRepository activityRepository,
            PlatGroupActivitySignupRepository activitySignupRepository,
            PlatformGroupSeasonScoreRepository seasonScoreRepository
    ) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.checkinRepository = checkinRepository;
        this.activityRepository = activityRepository;
        this.activitySignupRepository = activitySignupRepository;
        this.seasonScoreRepository = seasonScoreRepository;
    }

    public Map<String, Object> overview() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDate weekStart = today.minusDays(6);

        long groupTotal = groupRepository.count();
        long publishedGroups = groupRepository.countByStatus("published");
        long membersTotal = memberRepository.count();
        long pendingMembers = memberRepository.countByStatus("pending");
        long postsTotal = postRepository.count();
        long postsToday = postRepository.countByCreatedAtGreaterThanEqual(todayStart);
        long weekCheckinUsers = 0;
        for (var g : groupRepository.findByStatusOrderByMemberCountDescCreatedAtDesc("published")) {
            weekCheckinUsers += checkinRepository.countDistinctUsersSince(g.getId(), weekStart);
        }
        long activitiesTotal = activityRepository.count();
        long signupsLast7d = activitySignupRepository.countByCreatedAtGreaterThanEqual(weekStart.atStartOfDay());
        long seasonEntries = seasonScoreRepository.count();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("groupTotal", groupTotal);
        out.put("publishedGroups", publishedGroups);
        out.put("membersTotal", membersTotal);
        out.put("pendingMembers", pendingMembers);
        out.put("postsTotal", postsTotal);
        out.put("postsToday", postsToday);
        out.put("weekActiveCheckinUsers", weekCheckinUsers);
        out.put("activitiesTotal", activitiesTotal);
        out.put("activitySignupsLast7d", signupsLast7d);
        out.put("seasonScoreEntries", seasonEntries);
        out.put("generatedAt", LocalDateTime.now().toString());
        return out;
    }
}
