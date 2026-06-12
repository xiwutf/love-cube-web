package com.lovecube.backend.services;

import com.lovecube.backend.entity.PlatGroupMember;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.PlatGroupActivityRepository;
import com.lovecube.backend.repository.PlatGroupActivitySignupRepository;
import com.lovecube.backend.repository.PlatGroupCheckinRepository;
import com.lovecube.backend.repository.PlatGroupMemberRepository;
import com.lovecube.backend.repository.PlatGroupNoticeRepository;
import com.lovecube.backend.repository.PlatGroupPostRepository;
import com.lovecube.backend.repository.PlatGroupRepository;
import com.lovecube.backend.repository.SpaceCampaignProgressRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SpaceStatsService {

    private static final int MEMBER_LIST_LIMIT = 20;

    private final PlatGroupRepository groupRepository;
    private final PlatGroupMemberRepository memberRepository;
    private final PlatGroupCheckinRepository checkinRepository;
    private final PlatGroupPostRepository postRepository;
    private final PlatGroupActivitySignupRepository signupRepository;
    private final PlatGroupNoticeRepository noticeRepository;
    private final PlatGroupActivityRepository activityRepository;
    private final SpaceCampaignProgressRepository campaignProgressRepository;
    private final SpaceCampaignService spaceCampaignService;
    private final UserRepository userRepository;
    private final AdminAuthService adminAuthService;

    public SpaceStatsService(
            PlatGroupRepository groupRepository,
            PlatGroupMemberRepository memberRepository,
            PlatGroupCheckinRepository checkinRepository,
            PlatGroupPostRepository postRepository,
            PlatGroupActivitySignupRepository signupRepository,
            PlatGroupNoticeRepository noticeRepository,
            PlatGroupActivityRepository activityRepository,
            SpaceCampaignProgressRepository campaignProgressRepository,
            SpaceCampaignService spaceCampaignService,
            UserRepository userRepository,
            AdminAuthService adminAuthService
    ) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.checkinRepository = checkinRepository;
        this.postRepository = postRepository;
        this.signupRepository = signupRepository;
        this.noticeRepository = noticeRepository;
        this.activityRepository = activityRepository;
        this.campaignProgressRepository = campaignProgressRepository;
        this.spaceCampaignService = spaceCampaignService;
        this.userRepository = userRepository;
        this.adminAuthService = adminAuthService;
    }

    public Map<String, Object> getSpaceStats(Long groupId, User viewer) {
        requireManagerOrSiteAdmin(groupId, viewer);
        groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since7d = now.minusDays(7);
        LocalDateTime since30d = now.minusDays(30);

        long totalMembers = memberRepository.countByGroupIdAndStatus(groupId, "approved");
        long pendingMembers = memberRepository.countByGroupIdAndStatus(groupId, "pending");
        long newMembers7d = memberRepository.countApprovedJoinedSince(groupId, since7d);
        long newMembers30d = memberRepository.countApprovedJoinedSince(groupId, since30d);

        Set<Long> active7d = collectActiveUserIds(groupId, since7d);
        Set<Long> active30d = collectActiveUserIds(groupId, since30d);
        double activeRate7d = totalMembers > 0
                ? Math.round(active7d.size() * 1000.0 / totalMembers) / 10.0
                : 0.0;

        Map<String, Object> campaignRaw = spaceCampaignService.buildActiveCampaignStatsSnapshot(groupId);

        long posts7d = postRepository.countByGroupIdAndStatusAndCreatedAtGreaterThanEqual(
                groupId, "published", since7d);
        long noticesCount = noticeRepository.countByGroupIdAndStatus(groupId, "published");
        long activitiesCount = activityRepository.countByGroupIdAndStatus(groupId, "published");
        long memberActivitiesSubmitted = activityRepository.countMemberActivityProposalsSubmitted(groupId);
        long memberActivitiesApproved = activityRepository.countMemberActivityProposalsApproved(groupId);
        long pendingActivityProposals = activityRepository.countByGroupIdAndStatus(groupId, "pending");

        List<PlatGroupMember> approvedMembers = memberRepository
                .findByGroupIdAndStatusOrderByJoinedAtAsc(groupId, "approved");
        Set<Long> allTimeActive = collectAllTimeActiveUserIds(groupId);

        long inactiveMembers7d = approvedMembers.stream()
                .filter(m -> !active7d.contains(m.getUserId()))
                .count();

        List<PlatGroupMember> noActivityMembers = approvedMembers.stream()
                .filter(m -> !allTimeActive.contains(m.getUserId()))
                .sorted(Comparator.comparing(PlatGroupMember::getJoinedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fallenBehindAll =
                (List<Map<String, Object>>) campaignRaw.getOrDefault("fallenBehindMembers", List.of());

        Map<String, Object> result = new LinkedHashMap<>();

        Map<String, Object> memberGrowth = new LinkedHashMap<>();
        memberGrowth.put("totalMembers", totalMembers);
        memberGrowth.put("newMembers7d", newMembers7d);
        memberGrowth.put("newMembers30d", newMembers30d);
        memberGrowth.put("pendingMembers", pendingMembers);
        result.put("memberGrowth", memberGrowth);

        Map<String, Object> activity = new LinkedHashMap<>();
        activity.put("activeMembers7d", active7d.size());
        activity.put("activeMembers30d", active30d.size());
        activity.put("activeRate7d", activeRate7d);
        result.put("activity", activity);

        Map<String, Object> campaign = new LinkedHashMap<>();
        campaign.put("activeCampaignTitle", campaignRaw.getOrDefault("activeCampaignTitle", ""));
        campaign.put("campaignParticipants", campaignRaw.getOrDefault("campaignParticipants", 0));
        campaign.put("campaignTodayCompleted", campaignRaw.getOrDefault("campaignTodayCompleted", 0));
        campaign.put("campaignCompletionRate", campaignRaw.getOrDefault("campaignCompletionRate", 0.0));
        campaign.put("fallenBehindCount", campaignRaw.getOrDefault("fallenBehindCount", 0));
        result.put("campaign", campaign);

        Map<String, Object> content = new LinkedHashMap<>();
        content.put("posts7d", posts7d);
        content.put("noticesCount", noticesCount);
        content.put("activitiesCount", activitiesCount);
        content.put("memberActivitiesSubmitted", memberActivitiesSubmitted);
        content.put("memberActivitiesApproved", memberActivitiesApproved);
        content.put("pendingActivityProposals", pendingActivityProposals);
        result.put("content", content);

        Map<String, Object> risks = new LinkedHashMap<>();
        risks.put("inactiveMembers7d", inactiveMembers7d);
        risks.put("joinedButNoActivityCount", noActivityMembers.size());
        risks.put("joinedButNoActivityMembers", toMemberList(noActivityMembers));
        risks.put("fallenBehindMembers", limitList(fallenBehindAll));
        result.put("risks", risks);

        return result;
    }

    private List<Map<String, Object>> toMemberList(List<PlatGroupMember> members) {
        List<Map<String, Object>> list = new ArrayList<>();
        int limit = Math.min(members.size(), MEMBER_LIST_LIMIT);
        for (int i = 0; i < limit; i++) {
            PlatGroupMember m = members.get(i);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("userId", m.getUserId());
            row.put("username", resolveDisplayName(m));
            row.put("joinedAt", m.getJoinedAt());
            list.add(row);
        }
        return list;
    }

    private List<Map<String, Object>> limitList(List<Map<String, Object>> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        return source.size() > MEMBER_LIST_LIMIT
                ? new ArrayList<>(source.subList(0, MEMBER_LIST_LIMIT))
                : new ArrayList<>(source);
    }

    private Set<Long> collectActiveUserIds(Long groupId, LocalDateTime since) {
        Set<Long> ids = new HashSet<>();
        ids.addAll(checkinRepository.findDistinctUserIdsSinceDate(groupId, since.toLocalDate()));
        ids.addAll(postRepository.findDistinctAuthorIdsSince(groupId, since));
        ids.addAll(signupRepository.findDistinctUserIdsSince(groupId, since));
        ids.addAll(campaignProgressRepository.findDistinctUserIdsForGroupSince(groupId, since));
        return ids;
    }

    private Set<Long> collectAllTimeActiveUserIds(Long groupId) {
        Set<Long> ids = new HashSet<>();
        ids.addAll(checkinRepository.findDistinctUserIdsAllTime(groupId));
        ids.addAll(postRepository.findDistinctAuthorIdsAllTime(groupId));
        ids.addAll(signupRepository.findDistinctUserIdsAllTime(groupId));
        ids.addAll(campaignProgressRepository.findDistinctUserIdsForGroupAllTime(groupId));
        return ids;
    }

    private String resolveDisplayName(PlatGroupMember m) {
        if (m.getMemberRealName() != null && !m.getMemberRealName().isBlank()) {
            return m.getMemberRealName();
        }
        return userRepository.findById(m.getUserId())
                .map(User::getUsername)
                .orElse("成员" + m.getUserId());
    }

    private void requireManagerOrSiteAdmin(Long groupId, User user) {
        if (adminAuthService.isAdmin(user)) {
            return;
        }
        memberRepository.findByGroupIdAndUserId(groupId, user.getUserid())
                .filter(m -> "approved".equals(m.getStatus())
                        && ("owner".equals(m.getRole()) || "admin".equals(m.getRole())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "需要团体管理权限"));
    }
}
