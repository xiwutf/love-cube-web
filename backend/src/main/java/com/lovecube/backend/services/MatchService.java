package com.lovecube.backend.services;

import com.lovecube.backend.models.MatchRecord;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.FellowshipProfileRepository;
import com.lovecube.backend.repository.MatchRecordRepository;
import com.lovecube.backend.repository.UserInteractionRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            return new ArrayList<>();
        }
        Integer effectiveGender = oppositeGender != null ? oppositeGender : gender;

        // 构建查询条件
        List<User> potentialMatches = userRepository.findAll().stream()
            .filter(user -> !user.getUserid().equals(userId))
            .filter(user -> !"DISABLED".equalsIgnoreCase(user.getUserStatus()))
            .filter(this::isVisibleInMatchPool)
            .filter(user -> minAge == null || user.getAge() >= minAge)
            .filter(user -> maxAge == null || user.getAge() <= maxAge)
            .filter(user -> effectiveGender == null || user.getGender().equals(effectiveGender))
            .filter(user -> location == null || user.getLocation().contains(location))
            .collect(Collectors.toList());

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


        // 仅为新用户创建匹配记录（跳过已存在的，避免重复写入）
        try {
            for (User user : potentialMatches) {
                boolean exists = !matchRecordRepository
                    .findByUserIdAndMatchedUserId(userId, user.getUserid()).isEmpty();
                if (exists) continue;
                MatchRecord record = new MatchRecord();
                record.setUserId(userId);
                record.setMatchedUserId(user.getUserid());
                record.setMatchScore(calculateMatchScore(currentUser, user));
                matchRecordRepository.save(record);
            }
        } catch (Exception e) {
            logger.warn("保存匹配记录部分失败（忽略，不影响返回结果）: {}", e.getMessage());
        }

        return potentialMatches;
    }

    private double calculateMatchScore(User user1, User user2) {
        double score = 0.0;
        
        // 年龄差异评分（年龄差越小分数越高）
        int ageDiff = Math.abs(user1.getAge() - user2.getAge());
        score += Math.max(0, 10 - ageDiff) * 2; // 最高20分
        
        // 地理位置评分
        if (user1.getLocation() != null && user2.getLocation() != null) {
            if (user1.getLocation().equals(user2.getLocation())) {
                score += 30; // 同一地区加30分
            } else if (user1.getLocation().length() >= 2 && user2.getLocation().length() >= 2 &&
                     user1.getLocation().substring(0, 2).equals(user2.getLocation().substring(0, 2))) {
                score += 15; // 同一省份加15分
            }
        }
        
        // 职业评分
        if (user1.getOccupation() != null && user2.getOccupation() != null &&
            user1.getOccupation().equals(user2.getOccupation())) {
            score += 15; // 相同职业加15分
        }
        
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
            return new ArrayList<>();
        }
        Integer effectiveGender = oppositeGender != null ? oppositeGender : gender;

        List<Long> actedIds    = userInteractionRepository.findActedUserIdsByFromUserId(currentUserId);
        List<Long> guardianIds = getGuardianUserIds();
        List<Long> blacklistIds = getBlacklistIds(currentUserId);
        boolean keepActedUsers = Boolean.TRUE.equals(includeActed);

        if (effectiveGender != null) {
            return userRepository.findByGenderAndUseridNot(effectiveGender, currentUserId)
                .stream()
                .filter(u -> !"DISABLED".equalsIgnoreCase(u.getUserStatus()))
                .filter(this::isVisibleInMatchPool)
                .filter(u -> keepActedUsers || !actedIds.contains(u.getUserid()))
                .filter(u -> !guardianIds.contains(u.getUserid()))
                .filter(u -> !blacklistIds.contains(u.getUserid()))
                .filter(u -> minAge == null || (u.getAge() != null && u.getAge() >= minAge))
                .filter(u -> maxAge == null || (u.getAge() != null && u.getAge() <= maxAge))
                .filter(u -> location == null || location.isBlank() || (u.getLocation() != null && u.getLocation().contains(location)))
                .collect(Collectors.toList());
        }
        return userRepository.findByUseridNot(currentUserId)
            .stream()
            .filter(u -> !"DISABLED".equalsIgnoreCase(u.getUserStatus()))
            .filter(this::isVisibleInMatchPool)
            .filter(u -> keepActedUsers || !actedIds.contains(u.getUserid()))
            .filter(u -> !guardianIds.contains(u.getUserid()))
            .filter(u -> !blacklistIds.contains(u.getUserid()))
            .filter(u -> minAge == null || (u.getAge() != null && u.getAge() >= minAge))
            .filter(u -> maxAge == null || (u.getAge() != null && u.getAge() <= maxAge))
            .filter(u -> location == null || location.isBlank() || (u.getLocation() != null && u.getLocation().contains(location)))
            .collect(Collectors.toList());
    }

    private Integer getOppositeGender(Integer gender) {
        if (gender == null) return null;
        if (gender.equals(1)) return 2;
        if (gender.equals(2)) return 1;
        return null;
    }

    private boolean isVisibleInMatchPool(User user) {
        return Boolean.TRUE.equals(user.getFellowshipEnabled())
                && Boolean.TRUE.equals(user.getFellowshipMatchVisible());
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
