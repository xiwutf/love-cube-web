package com.lovecube.backend.repository;

import com.lovecube.backend.entity.Dynamic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DynamicRepository extends JpaRepository<Dynamic, Long> {
    
    /**
     * 获取未删除的动态列表（分页）
     */
    Page<Dynamic> findByIsDeletedFalseAndSceneTypeOrderByCreatedAtDesc(Pageable pageable, String sceneType);
    
    /**
     * 获取指定用户的动态列表
     */
    Page<Dynamic> findByUserIdAndIsDeletedFalseAndSceneTypeOrderByCreatedAtDesc(Long userId, String sceneType, Pageable pageable);
    
    /**
     * 获取动态详情
     */
    Dynamic findByIdAndIsDeletedFalseAndSceneType(Long id, String sceneType);
    
    /**
     * 获取用户动态数量
     */
    Long countByUserIdAndIsDeletedFalseAndSceneType(Long userId, String sceneType);

    /**
     * 获取未删除动态总数
     */
    long countByIsDeletedFalseAndSceneType(String sceneType);

    boolean existsByUserIdAndContentAndSceneTypeAndIsDeletedFalse(Long userId, String content, String sceneType);

    boolean existsByUserIdAndMarkerAndSceneTypeAndIsDeletedFalse(Long userId, String marker, String sceneType);

    boolean existsByMarkerAndSceneTypeAndIsDeletedFalse(String marker, String sceneType);

    /**
     * 获取热门动态（按点赞数排序）
     */
    @Query("SELECT d FROM Dynamic d WHERE d.isDeleted = false AND d.sceneType = :sceneType ORDER BY d.likeCount DESC, d.createdAt DESC")
    Page<Dynamic> findHotDynamics(@Param("sceneType") String sceneType, Pageable pageable);

    long countByIsDeletedFalseAndSceneTypeAndCreatedAtGreaterThanEqual(String sceneType, LocalDateTime since);

    long countByIsDeletedFalseAndSceneTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            String sceneType,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT COALESCE(SUM(d.commentCount), 0) FROM Dynamic d WHERE d.isDeleted = false AND d.sceneType = :sceneType AND d.createdAt >= :start AND d.createdAt < :end")
    long sumCommentCountVisibleBetween(@Param("sceneType") String sceneType, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(DISTINCT d.userId) FROM Dynamic d WHERE d.isDeleted = false AND d.sceneType = :sceneType AND d.createdAt >= :start AND d.createdAt < :end")
    long countDistinctActiveUsersBetween(@Param("sceneType") String sceneType, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(d.commentCount), 0) FROM Dynamic d WHERE d.isDeleted = false AND d.sceneType = :sceneType")
    long sumCommentCountVisible(@Param("sceneType") String sceneType);

    @Query("SELECT COALESCE(SUM(d.likeCount), 0) FROM Dynamic d WHERE d.isDeleted = false AND d.sceneType = :sceneType")
    long sumLikeCountVisible(@Param("sceneType") String sceneType);
} 
