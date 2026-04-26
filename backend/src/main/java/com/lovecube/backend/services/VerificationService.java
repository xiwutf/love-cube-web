package com.lovecube.backend.services;

import com.lovecube.backend.entity.UserVerification;
import com.lovecube.backend.repository.UserVerificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VerificationService {

    private final UserVerificationRepository userVerificationRepository;

    public VerificationService(UserVerificationRepository userVerificationRepository) {
        this.userVerificationRepository = userVerificationRepository;
    }

    public UserVerification submitVerification(Long userId, String verifyType, String submitData) {
        // Reject if there is already a pending entry for this type
        userVerificationRepository
            .findTopByUserIdAndVerifyTypeOrderBySubmittedAtDesc(userId, verifyType)
            .ifPresent(existing -> {
                if ("pending".equalsIgnoreCase(existing.getStatus())) {
                    throw new IllegalStateException("该认证类型已有待审核申请");
                }
            });

        UserVerification v = new UserVerification();
        v.setUserId(userId);
        v.setVerifyType(verifyType);
        v.setSubmitData(submitData);
        v.setStatus("pending");
        v.setSubmittedAt(LocalDateTime.now());
        return userVerificationRepository.save(v);
    }

    public List<UserVerification> getMyVerifications(Long userId) {
        return userVerificationRepository.findByUserIdOrderBySubmittedAtDesc(userId);
    }

    public Map<String, Object> getStatusSummary(Long userId) {
        List<UserVerification> list = userVerificationRepository.findByUserIdOrderBySubmittedAtDesc(userId);
        boolean photoVerified = list.stream()
            .anyMatch(v -> "PHOTO".equalsIgnoreCase(v.getVerifyType()) && "approved".equalsIgnoreCase(v.getStatus()));
        boolean realnameVerified = list.stream()
            .anyMatch(v -> "REALNAME".equalsIgnoreCase(v.getVerifyType()) && "approved".equalsIgnoreCase(v.getStatus()));
        Map<String, Object> result = new HashMap<>();
        result.put("photoVerified", photoVerified);
        result.put("realnameVerified", realnameVerified);
        return result;
    }

    /** Batch lookup for enriching user lists without N+1. */
    public Map<Long, Map<String, Boolean>> getBatchSummary(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        List<UserVerification> approved = userVerificationRepository.findApprovedByUserIds(userIds);
        Map<Long, Map<String, Boolean>> result = new HashMap<>();
        for (Long uid : userIds) {
            result.put(uid, new HashMap<>(Map.of("photoVerified", false, "realnameVerified", false)));
        }
        for (UserVerification v : approved) {
            Map<String, Boolean> entry = result.computeIfAbsent(v.getUserId(),
                k -> new HashMap<>(Map.of("photoVerified", false, "realnameVerified", false)));
            if ("PHOTO".equalsIgnoreCase(v.getVerifyType())) entry.put("photoVerified", true);
            if ("REALNAME".equalsIgnoreCase(v.getVerifyType())) entry.put("realnameVerified", true);
        }
        return result;
    }
}
