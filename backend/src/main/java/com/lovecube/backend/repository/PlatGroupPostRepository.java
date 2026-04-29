package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatGroupPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatGroupPostRepository extends JpaRepository<PlatGroupPost, Long> {

    List<PlatGroupPost> findByGroupIdAndStatusOrderByCreatedAtDesc(Long groupId, String status);

    List<PlatGroupPost> findTop20ByStatusOrderByCreatedAtDesc(String status);
}
