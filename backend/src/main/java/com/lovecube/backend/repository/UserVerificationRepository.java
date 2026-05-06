package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserVerificationRepository extends JpaRepository<UserVerification, Long> {
    List<UserVerification> findByUserIdIn(Collection<Long> userIds);
    Optional<UserVerification> findTopByUserIdOrderBySubmittedAtDesc(Long userId);
    List<UserVerification> findByUserIdOrderBySubmittedAtDesc(Long userId);
    Optional<UserVerification> findTopByUserIdAndVerifyTypeOrderBySubmittedAtDesc(Long userId, String verifyType);
    @Query("SELECT v FROM UserVerification v WHERE v.userId IN :ids AND v.status = 'approved'")
    List<UserVerification> findApprovedByUserIds(@Param("ids") Collection<Long> ids);
    List<UserVerification> findAllByOrderBySubmittedAtDesc();
    long countByStatus(String status);
}
