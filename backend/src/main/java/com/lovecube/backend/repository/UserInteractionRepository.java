package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserInteraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
    
    /**
     * 获取用户收到的所有互动（按时间倒序）
     */
    List<UserInteraction> findByToUserIdOrderByCreatedAtDesc(Long toUserId);
    
    /**
     * 获取用户收到的所有互动（分页）
     */
    List<UserInteraction> findByToUserIdOrderByCreatedAtDesc(Long toUserId, Pageable pageable);

    /**
     * 消息中心「互动」：仅展示正向类型（不含跳过等）
     */
    List<UserInteraction> findByToUserIdAndInteractionTypeInOrderByCreatedAtDesc(
            Long toUserId, Collection<UserInteraction.InteractionType> types, Pageable pageable);

    /**
     * 正向互动未读数（与消息中心列表口径一致）
     */
    Long countByToUserIdAndIsReadFalseAndInteractionTypeIn(
            Long toUserId, Collection<UserInteraction.InteractionType> types);
    
    /**
     * 获取用户收到的未读互动数量
     */
    Long countByToUserIdAndIsReadFalse(Long toUserId);
    
    /**
     * 获取用户收到的未读互动
     */
    List<UserInteraction> findByToUserIdAndIsReadFalseOrderByCreatedAtDesc(Long toUserId);
    
    /**
     * 获取特定类型的互动
     */
    List<UserInteraction> findByToUserIdAndInteractionTypeOrderByCreatedAtDesc(
        Long toUserId, UserInteraction.InteractionType interactionType);

    /**
     * 获取用户发起的指定类型互动
     */
    List<UserInteraction> findByFromUserIdAndInteractionTypeOrderByCreatedAtDesc(
        Long fromUserId, UserInteraction.InteractionType interactionType);
    
    /**
     * 获取两个用户之间的特定互动
     */
    Optional<UserInteraction> findByFromUserIdAndToUserIdAndInteractionTypeAndTargetId(
        Long fromUserId, Long toUserId, UserInteraction.InteractionType interactionType, Long targetId);
    
    /**
     * 检查用户是否已经对目标进行了某种互动
     */
    boolean existsByFromUserIdAndToUserIdAndInteractionTypeAndTargetId(
        Long fromUserId, Long toUserId, UserInteraction.InteractionType interactionType, Long targetId);
    
    /**
     * 检查用户是否关注了另一个用户
     */
    boolean existsByFromUserIdAndToUserIdAndInteractionType(
        Long fromUserId, Long toUserId, UserInteraction.InteractionType interactionType);
    
    /**
     * 获取用户的关注列表
     */
    @Query("SELECT ui FROM UserInteraction ui WHERE ui.fromUserId = :userId AND ui.interactionType = 'FOLLOW'")
    List<UserInteraction> findFollowingList(@Param("userId") Long userId);
    
    /**
     * 获取用户的粉丝列表
     */
    @Query("SELECT ui FROM UserInteraction ui WHERE ui.toUserId = :userId AND ui.interactionType = 'FOLLOW'")
    List<UserInteraction> findFollowersList(@Param("userId") Long userId);
    
    /**
     * 获取今日收到的互动数量
     */
    @Query("SELECT COUNT(ui) FROM UserInteraction ui WHERE ui.toUserId = :userId AND ui.createdAt >= :startOfDay")
    Long countTodayInteractions(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);
    
    /**
     * 获取用户收到的点赞数量
     */
    Long countByToUserIdAndInteractionType(Long toUserId, UserInteraction.InteractionType interactionType);

    Long countByFromUserIdAndInteractionType(Long fromUserId, UserInteraction.InteractionType interactionType);

    Long countByInteractionTypeAndCreatedAtGreaterThanEqual(
        UserInteraction.InteractionType interactionType,
        LocalDateTime createdAt);

    Long countByInteractionTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
        UserInteraction.InteractionType interactionType,
        LocalDateTime start,
        LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT ui.fromUserId) FROM UserInteraction ui WHERE ui.createdAt >= :start AND ui.createdAt < :end")
    long countDistinctActiveUsersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * 获取当前用户已操作过的目标用户 ID（like/superlike/skip），用于过滤推荐列表
     */
    @Query("SELECT DISTINCT ui.toUserId FROM UserInteraction ui WHERE ui.fromUserId = :userId " +
           "AND ui.interactionType IN ('LIKE', 'SUPER_LIKE', 'FOLLOW', 'SKIP')")
    List<Long> findActedUserIdsByFromUserId(@Param("userId") Long userId);

    /**
     * 认识页：当前用户发出的喜欢 / 超级喜欢 / 跳过记录（按时间倒序，数据库分页）
     */
    Page<UserInteraction> findByFromUserIdAndInteractionTypeInOrderByCreatedAtDesc(
            Long fromUserId, Collection<UserInteraction.InteractionType> types, Pageable pageable);

    /**
     * 批量标记互动为已读
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserInteraction ui SET ui.isRead = true WHERE ui.toUserId = :userId AND ui.isRead = false")
    void markAllAsRead(@Param("userId") Long userId);
    
    /**
     * 删除特定互动（如取消点赞、取消关注）
     */
    void deleteByFromUserIdAndToUserIdAndInteractionTypeAndTargetId(
        Long fromUserId, Long toUserId, UserInteraction.InteractionType interactionType, Long targetId);
    
    /**
     * 删除关注记录
     */
    void deleteByFromUserIdAndToUserIdAndInteractionType(
        Long fromUserId, Long toUserId, UserInteraction.InteractionType interactionType);
} 
