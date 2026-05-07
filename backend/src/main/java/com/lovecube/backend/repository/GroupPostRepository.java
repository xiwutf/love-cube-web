package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GroupPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupPostRepository extends JpaRepository<GroupPost, String> {

    List<GroupPost> findByGroupIdOrderByCreatedAtDesc(String groupId);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);
}
