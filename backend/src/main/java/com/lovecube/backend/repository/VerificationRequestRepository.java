package com.lovecube.backend.repository;

import com.lovecube.backend.entity.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, String> {
    List<VerificationRequest> findByUserIdOrderBySubmittedAtDesc(Long userId);

    List<VerificationRequest> findByUserIdIn(Collection<Long> userIds);

    long countByStatusIgnoreCase(String status);
}
