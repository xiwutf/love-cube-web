package com.lovecube.backend.repository;

import com.lovecube.backend.entity.HelpReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HelpReplyRepository extends JpaRepository<HelpReply, Long> {

    List<HelpReply> findByRequestIdOrderByCreatedAtAsc(Long requestId);

    Optional<HelpReply> findByRequestIdAndUserId(Long requestId, Long userId);

    boolean existsByRequestIdAndUserId(Long requestId, Long userId);

    Page<HelpReply> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<HelpReply> findByRequestIdAndStatus(Long requestId, String status);

    Optional<HelpReply> findByRequestIdAndUserIdAndStatus(Long requestId, Long userId, String status);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);
}
