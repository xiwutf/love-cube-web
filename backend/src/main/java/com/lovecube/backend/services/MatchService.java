package com.lovecube.backend.services;

import com.lovecube.backend.models.MatchRecord;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.FellowshipProfileRepository;
import com.lovecube.backend.repository.MatchRecordRepository;
import com.lovecube.backend.repository.UserInteractionRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.repository.UserGrowthRepository;
import com.lovecube.backend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchService
{
    private static final Logger logger = LoggerFactory.getLogger(MatchService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRecordRepository matchRecordRepository;

    @Autowired
    private UserInteractionRepository userInteractionRepository;

    @Autowired
    private FellowshipProfileRepository fellowshipProfileRepository;

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private UnifiedProfileService unifiedProfileService;

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private UserGrowthRepository userGrowthRepository;

    @Transactional
    public List<User> findMatches(Long userId, Integer minAge, Integer maxAge, Integer gender, String location) {
        logger.info("开始查找匹配 - userId: {}, minAge: {}, maxAge: {}, gender: {}, location: {}", 
            userId, minAge, maxAge, gender, location);

        // 参数验证
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("无效的用户ID");
        }

        if (minAge != null && maxAge != null && minAge > maxAge) {
            throw new IllegalArgumentException("最小年龄不能大于最大年龄");
        }

        // 获取当前用户
        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Integer oppositeGender = getOppositeGender(currentUser.getGender());
        if (gender != null && oppositeGender != null && !gender.equals(oppositeGender)) {
            logger.debug("忽略与异性策略冲突的 gender 参数: 传入 {} 有效异性 {}", gender, oppositeGender);
        }
        Integer effectiveGender = oppositeGender != null ? oppositeGender : gender;

        List<User> potentialMatches = userRepository.findMatchCandidates(
                userId, effectiveGender, minAge, maxAge, location);

        logger.info("初步筛选 - 找到 {} 个潜在匹配", potentialMatches.size());

        // 获取已匹配用户列表
        final List<Long> matchedUserIds;
        try {
            matchedUserIds = matchRecordRepository.findByUserId(userId)
                    .stream()
                    .map(MatchRecord::getMatchedUserId)
                    .collect(Collectors.toList());
            logger.info("已匹配用户数量: {}", matchedUserIds.size());
        } catch (Exception e) {
            logger.error("获取已匹配用户失败", e);
            throw new RuntimeException("获取匹配记录失败: " + e.getMessage());
        }


        // 批量查出已存在的匹配记录，再批量写入新记录（避免 N+1）
        try {
            List<Long> candidateIds = potentialMatches.stream()
                    .map(User::getUserid)
                    .collect(Collectors.toList());
            Set<Long> existingIds = candidateIds.isEmpty()
                    ? new HashSet<>()
                    : new HashSet<>((Collection<Long>) matchRecordRepository
                            .findExistingMatchedUserIds(userId, candidateIds));

            List<MatchRecord> toSave = potentialMatches.stream()
                    .filter(u -> !existingIds.contains(u.getUserid()))
                    .map(u -> {
                        MatchRecord r = new MatchRecord();
                        r.setUserId(userId);
                        r.setMatchedUserId(u.getUserid());
                        r.setMatchScore(calculateMatchScore(currentUser, u));
                        return r;
                    })
                    .collect(Collectors.toList());
            if (!toSave.isEmpty()) {
                matchRecordRepository.saveAll(toSave);
            }
        } catch (Exception e) {
            logger.warn("保存匹配记录部分失败（忽略，不影响返回结果）: {}", e.getMessage());
        }

        List<Long> candidateIds = potentialMatches.stream().map(User::getUserid).collect(Collectors.toList());
        Map<Long, Map<String, Boolean>> verifyMap = verificationService.getBatchSummary(candidateIds);
        Map<Long, Integer> levelMap = userGrowthRepository.findByUserIdIn(candidateIds).stream()
                .collect(Collectors.toMap(
                        g -> g.getUserId(),
                        g -> g.getLevel() == null ? 1 : g.getLevel(),
                        (a, b) -> a));

        return potentialMatches.stream()
                .sorted(Comparator.comparingDouble((User u) -> rankCandidate(u, verifyMap, levelMap)).reversed())
                .collect(Collectors.toList());
    }

    private double calculateMatchScore(User user1, User user2) {
        double score = 0.0;

        int ageDiff = Math.abs(user1.getAge() - user2.getAge());
        score += Math.max(0, 10 - ageDiff) * 2;

        if (user1.getLocation() != null && user2.getLocation() != null) {
            if (user1.getLocation().equals(user2.getLocation())) {
                score += 30;
            } else if (user1.getLocation().length() >= 2 && user2.getLocation().length() >= 2 &&
                     user1.getLocation().substring(0, 2).equals(user2.getLocation().substring(0, 2))) {
                score += 15;
            }
        }

        if (user1.getOccupation() != null && user2.getOccupation() != null &&
            user1.getOccupation().equals(user2.getOccupation())) {
            score += 15;
        }

        Map<Long, Map<String, Boolean>> verifyMap = verificationService.getBatchSummary(
                List.of(user1.getUserid(), user2.getUserid()));
        score += unifiedProfileService.computeRecommendRankForUser(
                user1, verifyMap.getOrDefault(user1.getUserid(), Map.of()));
        score += unifiedProfileService.computeRecommendRankForUser(
                user2, verifyMap.getOrDefault(user2.getUserid(), Map.of()));

        return score;
    }

    /**
     * 获取当前用户ID
     */
    public Long getCurrentUserId(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid != null) {
            User user = userRepository.findByOpenid(openid);
            return user != null ? user.getUserid() : null;
        }
        return null;
    }

    /**
     * 获取推荐列表：排除自己、已操作用户和家长账号
     */
    public List<User> getAllUsers(Long currentUserId, Integer gender) {
        return getAllUsers(currentUserId, gender, null, null, null, false);
    }

    /**
     * 获取推荐列表（支持筛选）：默认按当前用户异性过滤
     */
    public List<User> getAllUsers(Long currentUserId, Integer gender, Integer minAge, Integer maxAge, String location) {
        return getAllUsers(currentUserId, gender, minAge, maxAge, location, false);
    }

    /**
     * 获取推荐/浏览列表（支持筛选）：默认按当前用户异性过滤。
     * includeActed=true 时会保留已互动用户，用于“全部异性浏览”场景。
     */
    public List<User> getAllUsers(Long currentUserId, Integer gender, Integer minAge, Integer maxAge, String location, Boolean includeActed) {
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        Integer oppositeGender = currentUser == null ? null : getOppositeGender(currentUser.getGender());
        if (gender != null && oppositeGender != null && !gender.equals(oppositeGender)) {
            logger.debug(
                    "列表性别参数 {} 与当前账号异性 {} 不一致，已忽略参数（仍按异性推荐）",
                    gender,
                    oppositeGender);
        }
        Integer effectiveGender = oppositeGender != null ? oppositeGender : gender;

        Set<Long> actedIds = new HashSet<>(userInteractionRepository.findActedUserIdsByFromUserId(currentUserId));
        List<Long> guardianIds = getGuardianUserIds();
        List<Long> blacklistIds = getBlacklistIds(currentUserId);
        Set<Long> excludedIds = new HashSet<>();
        excludedIds.addAll(guardianIds);
        excludedIds.addAll(blacklistIds);
        boolean keepActedUsers = Boolean.TRUE.equals(includeActed);

        return userRepository.findMatchCandidates(currentUserId, effectiveGender, minAge, maxAge, location)
                .stream()
                .filter(u -> keepActedUsers || !actedIds.contains(u.getUserid()))
                .filter(u -> !excludedIds.contains(u.getUserid()))
                .collect(Collectors.toList());
    }

    /**
     * 匹配列表（数据库分页 + 排序）：避免先拉全量再在内存分页。
     */
    public Page<User> getAllUsersPage(
            Long currentUserId,
            Integer gender,
            Integer minAge,
            Integer maxAge,
            String location,
            Boolean includeActed,
            boolean verifiedOnly,
            int pageIndex,
            int pageSize) {
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        Integer oppositeGender = currentUser == null ? null : getOppositeGender(currentUser.getGender());
        if (gender != null && oppositeGender != null && !gender.equals(oppositeGender)) {
            logger.debug(
                    "列表性别参数 {} 与当前账号异性 {} 不一致，已忽略参数（仍按异性推荐）",
                    gender,
                    oppositeGender);
        }
        Integer effectiveGender = oppositeGender != null ? oppositeGender : gender;
        int genderFilter = effectiveGender != null ? effectiveGender : -999;
        int minAgeFilter = minAge != null ? minAge : -1;
        int maxAgeFilter = maxAge != null ? maxAge : -1;
        String locationPattern = (location == null || location.isBlank())
                ? null
                : ("%" + location.trim() + "%");
        int includeActedInt = Boolean.TRUE.equals(includeActed) ? 1 : 0;
        int verifiedOnlyInt = verifiedOnly ? 1 : 0;
        Pageable pageable = PageRequest.of(Math.max(pageIndex, 0), pageSize);
        Page<User> rawPage = userRepository.findMatchCandidatesPage(
                currentUserId,
                genderFilter,
                minAgeFilter,
                maxAgeFilter,
                locationPattern,
                includeActedInt,
                verifiedOnlyInt,
                pageable);
        List<User> content = rawPage.getContent();
        if (content.isEmpty()) {
            return rawPage;
        }
        List<Long> userIds = content.stream().map(User::getUserid).collect(Collectors.toList());
        Map<Long, Map<String, Boolean>> verifyMap = verificationService.getBatchSummary(userIds);
        Map<Long, Integer> levelMap = userGrowthRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(
                        g -> g.getUserId(),
                        g -> g.getLevel() == null ? 1 : g.getLevel(),
                        (a, b) -> a));
        List<User> sorted = content.stream()
                .sorted(Comparator.comparingDouble((User u) -> rankCandidate(u, verifyMap, levelMap)).reversed())
                .collect(Collectors.toList());
        return new PageImpl<>(sorted, pageable, rawPage.getTotalElements());
    }

    private double rankCandidate(User user, Map<Long, Map<String, Boolean>> verifyMap, Map<Long, Integer> levelMap) {
        Map<String, Boolean> badges = verifyMap.getOrDefault(user.getUserid(), Map.of());
        int completionRate = unifiedProfileService.computeFellowshipCompletionRateForUser(user);
        boolean verified = Boolean.TRUE.equals(badges.get("photoVerified"))
                || Boolean.TRUE.equals(badges.get("realnameVerified"));
        int growthLevel = levelMap.getOrDefault(user.getUserid(), 1);
        double score = FellowshipProfileCompletion.computeRecommendRank(completionRate, verified, growthLevel);
        if (user.getProfilePhoto() != null && !user.getProfilePhoto().isBlank()) {
            score += 5;
        }
        return score;
    }

    private Integer getOppositeGender(Integer gender) {
        if (gender == null) return null;
        if (gender.equals(1)) return 2;
        if (gender.equals(2)) return 1;
        return null;
    }

    private List<Long> getBlacklistIds(Long userId) {
        try {
            return blacklistService.getBlockedAndBlockerIds(userId);
        } catch (Exception e) {
            logger.warn("获取黑名单失败（忽略，不影响匹配）: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    private List<Long> getGuardianUserIds() {
        try {
            return fellowshipProfileRepository.findUserIdsByIdentityRoleIn(
                    List.of("guardian_son", "guardian_daughter"));
        } catch (Exception e) {
            logger.warn("获取家长账号列表失败（忽略，不影响匹配）: {}", e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 互赞配对成功后写入双向匹配记录（幂等）
     */
    @Transactional
    public void ensureMutualMatchRecord(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null || userId.equals(targetUserId)) {
            return;
        }
        if (!checkMatchExists(userId, targetUserId)) {
            createMatch(userId, targetUserId);
        }
        if (!checkMatchExists(targetUserId, userId)) {
            createMatch(targetUserId, userId);
        }
    }

    /**
     * 创建匹配记录
     */
    @Transactional
    public void createMatch(Long userId, Long targetUserId) {
        MatchRecord match = new MatchRecord();
        match.setUserId(userId);
        match.setMatchedUserId(targetUserId);
        match.setMatchScore(calculateMatchScore(
            userRepository.findById(userId).orElseThrow(),
            userRepository.findById(targetUserId).orElseThrow()
        ));
        matchRecordRepository.save(match);
    }

    /**
     * 检查两个用户之间是否已经匹配
     */
    public boolean checkMatchExists(Long userId, Long targetUserId) {
        return !matchRecordRepository.findByUserIdAndMatchedUserId(userId, targetUserId).isEmpty();
    }

    /**
     * 获取匹配统计信息
     */
    public Map<String, Object> getMatchStats(Long userId) {
        List<MatchRecord> matches = matchRecordRepository.findByUserId(userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMatches", matches.size());
        
        // 按匹配分数分组统计
        Map<String, Long> scoreDistribution = matches.stream()
            .collect(Collectors.groupingBy(
                match -> {
                    double score = match.getMatchScore();
                    if (score >= 80) return "高匹配度";
                    else if (score >= 60) return "中等匹配度";
                    else return "低匹配度";
                },
                Collectors.counting()
            ));
        stats.put("scoreDistribution", scoreDistribution);
        
        return stats;
    }
}
