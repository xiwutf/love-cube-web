package com.lovecube.backend.repository;

import com.lovecube.backend.entity.InviteRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InviteRecordRepository extends JpaRepository<InviteRecord, Long>, JpaSpecificationExecutor<InviteRecord> {
    long countByInviterUserIdAndStatus(Long inviterUserId, String status);

    List<InviteRecord> findByInviterUserIdAndStatusOrderByCreatedAtDesc(Long inviterUserId, String status);
}

