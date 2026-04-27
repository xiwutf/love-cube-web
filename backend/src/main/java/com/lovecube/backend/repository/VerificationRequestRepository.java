package com.lovecube.backend.repository;

import com.lovecube.backend.entity.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, String> {
    List<VerificationRequest> findByUserIdOrderBySubmittedAtDesc(Long userId);

    long countByStatusIgnoreCase(String status);
}
