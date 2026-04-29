package com.lovecube.backend.services;

import com.lovecube.backend.entity.UserInteraction;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserInteractionRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserInteractionService {

    /** 消息中心「互动」Tab：只展示正向反馈（喜欢、超级喜欢、关注、礼物、评论），不展示「跳过」等 */
    private static final List<UserInteraction.InteractionType> INTERACTION_INBOX_TYPES = List.of(
            UserInteraction.InteractionType.LIKE,
            UserInteraction.InteractionType.SUPER_LIKE,
            UserInteraction.InteractionType.FOLLOW,
            UserInteraction.InteractionType.GIFT,
            UserInteraction.InteractionType.COMMENT
    );

    @Autowired
    private UserInteractionRepository interactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UnifiedProfileService unifiedProfileService;

    public UserInteraction createInteraction(Long fromUserId, Long toUserId,
                                             UserInteraction.InteractionType type,
                                             UserInteraction.TargetType targetType,
                                             Long targetId, String content) {
        if (targetId != null) {
            boolean exists = interactionRepository.existsByFromUserIdAndToUserIdAndInteractionTypeAndTargetId(
                fromUserId, toUserId, type, targetId);
            if (exists) {
                throw new RuntimeException("已经存在相同的互动记录");
            }
        } else {
            boolean exists = interactionRepository.existsByFromUserIdAndToUserIdAndInteractionType(
                fromUserId, toUserId, type);
            if (exists) {
                throw new RuntimeException("已经关注该用户");
            }
        }

        UserInteraction interaction = new UserInteraction();
        interaction.setFromUserId(fromUserId);
        interaction.setToUserId(toUserId);
        interaction.setInteractionType(type);
        interaction.setTargetType(targetType);
        interaction.setTargetId(targetId);
        interaction.setContent(content);

        return interactionRepository.save(interaction);
    }

    public void removeInteraction(Long fromUserId, Long toUserId,
                                  UserInteraction.InteractionType type, Long targetId) {
        if (targetId != null) {
            interactionRepository.deleteByFromUserIdAndToUserIdAndInteractionTypeAndTargetId(
                fromUserId, toUserId, type, targetId);
        } else {
            interactionRepository.deleteByFromUserIdAndToUserIdAndInteractionType(
                fromUserId, toUserId, type);
        }
    }

    public List<Map<String, Object>> getInteractionList(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<UserInteraction> interactions = interactionRepository
                .findByToUserIdAndInteractionTypeInOrderByCreatedAtDesc(userId, INTERACTION_INBOX_TYPES, pageable);
        List<Long> fromIds = interactions.stream()
                .map(UserInteraction::getFromUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Map<String, String>> senderSummaries = unifiedProfileService.buildInboxSenderSummaries(fromIds);
        return interactions.stream()
                .map(i -> convertToInteractionDTO(i, senderSummaries))
                .collect(Collectors.toList());
    }

    public Long getUnreadInteractionCount(Long userId) {
        Long n = interactionRepository.countByToUserIdAndIsReadFalseAndInteractionTypeIn(userId, INTERACTION_INBOX_TYPES);
        return n != null ? n : 0L;
    }

    public void markAllInteractionsAsRead(Long userId) {
        interactionRepository.markAllAsRead(userId);
    }

    public boolean isFollowing(Long fromUserId, Long toUserId) {
        return interactionRepository.existsByFromUserIdAndToUserIdAndInteractionType(
            fromUserId, toUserId, UserInteraction.InteractionType.FOLLOW);
    }

    public boolean isLiked(Long fromUserId, Long toUserId, Long targetId) {
        return interactionRepository.existsByFromUserIdAndToUserIdAndInteractionTypeAndTargetId(
            fromUserId, toUserId, UserInteraction.InteractionType.LIKE, targetId);
    }

    public Long getFollowingCount(Long userId) {
        return (long) interactionRepository.findFollowingList(userId).size();
    }

    public Long getFollowersCount(Long userId) {
        return (long) interactionRepository.findFollowersList(userId).size();
    }

    public Long getLikesCount(Long userId) {
        return interactionRepository.countByToUserIdAndInteractionType(
            userId, UserInteraction.InteractionType.LIKE);
    }

    public Long getTodayInteractionCount(Long userId) {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        return interactionRepository.countTodayInteractions(userId, startOfDay);
    }

    public List<Map<String, Object>> getSentLikeUsers(Long userId) {
        List<UserInteraction> likes = interactionRepository.findByFromUserIdAndInteractionTypeOrderByCreatedAtDesc(
            userId, UserInteraction.InteractionType.LIKE);
        return buildUserSummaryList(likes, true);
    }

    public List<Map<String, Object>> getMutualLikeUsers(Long userId) {
        List<UserInteraction> likes = interactionRepository.findByFromUserIdAndInteractionTypeOrderByCreatedAtDesc(
            userId, UserInteraction.InteractionType.LIKE);
        List<UserInteraction> mutual = new ArrayList<>();
        for (UserInteraction like : likes) {
            Long targetUserId = like.getToUserId();
            boolean reverseLike = interactionRepository.existsByFromUserIdAndToUserIdAndInteractionTypeAndTargetId(
                targetUserId, userId, UserInteraction.InteractionType.LIKE, userId);
            if (reverseLike) {
                mutual.add(like);
            }
        }
        return buildUserSummaryList(mutual, true);
    }

    public List<Map<String, Object>> getReceivedLikeUsers(Long userId) {
        List<UserInteraction> likes = interactionRepository.findByToUserIdAndInteractionTypeOrderByCreatedAtDesc(
            userId, UserInteraction.InteractionType.LIKE);
        Map<Long, Map<String, Object>> users = new LinkedHashMap<>();
        for (UserInteraction interaction : likes) {
            Long fromUserId = interaction.getFromUserId();
            if (users.containsKey(fromUserId)) continue;
            User fromUser = userRepository.findById(fromUserId).orElse(null);
            if (fromUser == null) continue;

            Map<String, Object> row = new HashMap<>();
            row.put("userId", fromUser.getUserid());
            row.put("nickname", fromUser.getUsername());
            row.put("avatar", fromUser.getProfilePhoto());
            row.put("age", fromUser.getAge());
            row.put("location", fromUser.getLocation());
            row.put("occupation", fromUser.getOccupation());
            row.put("createdAt", interaction.getCreatedAt());
            users.put(fromUserId, row);
        }
        return new ArrayList<>(users.values());
    }

    public List<Map<String, Object>> getFollowingUsers(Long userId) {
        List<UserInteraction> follows = interactionRepository.findByFromUserIdAndInteractionTypeOrderByCreatedAtDesc(
            userId, UserInteraction.InteractionType.FOLLOW);
        return buildUserSummaryList(follows, false);
    }

    public UserInteraction followUser(Long fromUserId, Long toUserId) {
        return createInteraction(fromUserId, toUserId, UserInteraction.InteractionType.FOLLOW,
            UserInteraction.TargetType.USER, null, null);
    }

    public void unfollowUser(Long fromUserId, Long toUserId) {
        removeInteraction(fromUserId, toUserId, UserInteraction.InteractionType.FOLLOW, null);
    }

    public UserInteraction likeUser(Long fromUserId, Long toUserId) {
        return createInteraction(fromUserId, toUserId, UserInteraction.InteractionType.LIKE,
            UserInteraction.TargetType.PROFILE, toUserId, null);
    }

    public void unlikeUser(Long fromUserId, Long toUserId) {
        removeInteraction(fromUserId, toUserId, UserInteraction.InteractionType.LIKE, toUserId);
    }

    public UserInteraction superLikeUser(Long fromUserId, Long toUserId) {
        // 幂等：已 superlike 则不重复创建
        boolean alreadySuperLiked = interactionRepository.existsByFromUserIdAndToUserIdAndInteractionType(
            fromUserId, toUserId, UserInteraction.InteractionType.SUPER_LIKE);
        if (alreadySuperLiked) return null;
        UserInteraction interaction = new UserInteraction();
        interaction.setFromUserId(fromUserId);
        interaction.setToUserId(toUserId);
        interaction.setInteractionType(UserInteraction.InteractionType.SUPER_LIKE);
        interaction.setTargetType(UserInteraction.TargetType.PROFILE);
        interaction.setTargetId(toUserId);
        return interactionRepository.save(interaction);
    }

    public void skipUser(Long fromUserId, Long toUserId) {
        boolean alreadySkipped = interactionRepository.existsByFromUserIdAndToUserIdAndInteractionType(
            fromUserId, toUserId, UserInteraction.InteractionType.SKIP);
        if (alreadySkipped) return;
        UserInteraction interaction = new UserInteraction();
        interaction.setFromUserId(fromUserId);
        interaction.setToUserId(toUserId);
        interaction.setInteractionType(UserInteraction.InteractionType.SKIP);
        interaction.setTargetType(UserInteraction.TargetType.USER);
        interactionRepository.save(interaction);
    }

    public boolean checkMutualLike(Long userId, Long targetUserId) {
        return interactionRepository.existsByFromUserIdAndToUserIdAndInteractionTypeAndTargetId(
            targetUserId, userId, UserInteraction.InteractionType.LIKE, userId);
    }

    public List<Long> getActedUserIds(Long userId) {
        return interactionRepository.findActedUserIdsByFromUserId(userId);
    }

    public UserInteraction sendGift(Long fromUserId, Long toUserId, Long giftId, Integer giftCount, String message) {
        UserInteraction interaction = createInteraction(fromUserId, toUserId, UserInteraction.InteractionType.GIFT,
            UserInteraction.TargetType.USER, null, message);
        interaction.setGiftId(giftId);
        interaction.setGiftCount(giftCount);
        return interactionRepository.save(interaction);
    }

    private Map<String, Object> convertToInteractionDTO(UserInteraction interaction,
                                                        Map<Long, Map<String, String>> senderSummaries) {
        Map<String, Object> dto = new HashMap<>();
        Long fromId = interaction.getFromUserId();

        dto.put("id", interaction.getId());
        dto.put("type", interaction.getInteractionType().name().toLowerCase());
        dto.put("action", getActionText(interaction.getInteractionType()));
        dto.put("time", interaction.getCreatedAt());
        dto.put("createdAt", interaction.getCreatedAt());
        dto.put("isRead", interaction.getIsRead());

        Map<String, String> summary = senderSummaries != null ? senderSummaries.get(fromId) : null;
        User fromUser = null;
        if (summary == null) {
            fromUser = userRepository.findById(fromId).orElse(null);
        }

        String nickname = "用户";
        String avatar = "/images/default-avatar.png";
        Long uid = fromId;

        if (summary != null) {
            nickname = Objects.toString(summary.get("nickname"), "").trim();
            if (nickname.isEmpty()) {
                nickname = "用户";
            }
            avatar = Objects.toString(summary.get("avatar"), "").trim();
            if (avatar.isEmpty()) {
                avatar = "/images/default-avatar.png";
            }
        } else if (fromUser != null) {
            uid = fromUser.getUserid();
            String u = fromUser.getUsername() != null ? fromUser.getUsername().trim() : "";
            nickname = u.isEmpty() ? "用户" : u;
            avatar = fromUser.getProfilePhoto() != null && !fromUser.getProfilePhoto().isBlank()
                    ? fromUser.getProfilePhoto()
                    : "/images/default-avatar.png";
        }

        if (summary != null || fromUser != null) {
            dto.put("userId", uid);
            dto.put("nickname", nickname);
            dto.put("avatar", avatar);

            Map<String, Object> fromUserMap = new LinkedHashMap<>();
            fromUserMap.put("userId", uid);
            fromUserMap.put("nickname", nickname);
            fromUserMap.put("avatar", avatar);
            dto.put("fromUser", fromUserMap);
        }

        switch (interaction.getInteractionType()) {
            case LIKE:
                dto.put("target", "赞了你");
                break;
            case SKIP:
                dto.put("target", "暂时略过了你");
                break;
            case SUPER_LIKE:
                dto.put("target", "超级喜欢你");
                break;
            case FOLLOW:
                dto.put("target", "关注了你");
                break;
            case GIFT:
                dto.put("target", "给你送了礼物");
                if (interaction.getContent() != null) dto.put("message", interaction.getContent());
                if (interaction.getGiftCount() != null) dto.put("giftCount", interaction.getGiftCount());
                break;
            case COMMENT:
                dto.put("target", "评论了你");
                if (interaction.getContent() != null) dto.put("content", interaction.getContent());
                break;
        }

        return dto;
    }

    private String getActionText(UserInteraction.InteractionType type) {
        switch (type) {
            case LIKE: return "点赞了你";
            case SUPER_LIKE: return "超级喜欢你";
            case FOLLOW: return "关注了你";
            case GIFT: return "送了礼物";
            case COMMENT: return "评论了你";
            default: return "与你互动";
        }
    }

    private List<Map<String, Object>> buildUserSummaryList(List<UserInteraction> interactions, boolean checkMutualLike) {
        Map<Long, Map<String, Object>> users = new LinkedHashMap<>();
        for (UserInteraction interaction : interactions) {
            Long targetUserId = interaction.getToUserId();
            if (users.containsKey(targetUserId)) continue;

            User targetUser = userRepository.findById(targetUserId).orElse(null);
            if (targetUser == null) continue;

            Map<String, Object> row = new HashMap<>();
            row.put("userId", targetUser.getUserid());
            row.put("nickname", targetUser.getUsername());
            row.put("avatar", targetUser.getProfilePhoto());
            row.put("age", targetUser.getAge());
            row.put("location", targetUser.getLocation());
            row.put("occupation", targetUser.getOccupation());
            row.put("createdAt", interaction.getCreatedAt());
            if (checkMutualLike) {
                boolean mutual = interactionRepository.existsByFromUserIdAndToUserIdAndInteractionTypeAndTargetId(
                    targetUserId, interaction.getFromUserId(), UserInteraction.InteractionType.LIKE, interaction.getFromUserId());
                row.put("mutualLiked", mutual);
            }
            users.put(targetUserId, row);
        }
        return new ArrayList<>(users.values());
    }
}
