package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GroupPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupPostLikeRepository extends JpaRepository<GroupPostLike, Long> {

    void deleteByPostId(String postId);

    Optional<GroupPostLike> findByPostIdAndUserId(String postId, Long userId);

    @Query("select l.postId from GroupPostLike l where l.userId = :userId and l.postId in :postIds")
    List<String> findPostIdsLikedByUser(@Param("userId") Long userId, @Param("postIds") List<String> postIds);
}
