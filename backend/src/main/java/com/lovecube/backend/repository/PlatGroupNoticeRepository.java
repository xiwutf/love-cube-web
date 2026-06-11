package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatGroupNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatGroupNoticeRepository extends JpaRepository<PlatGroupNotice, Long> {

    List<PlatGroupNotice> findByGroupIdAndStatusOrderByCreatedAtDesc(Long groupId, String status);

    long countByGroupIdAndStatus(Long groupId, String status);
}
