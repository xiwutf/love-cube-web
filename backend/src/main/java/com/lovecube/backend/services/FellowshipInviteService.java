package com.lovecube.backend.services;

import com.lovecube.backend.entity.InviteRecord;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.InviteRecordRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FellowshipInviteService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String RANDOM_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    private final InviteRecordRepository inviteRecordRepository;
    private final UserRepository userRepository;

    public FellowshipInviteService(InviteRecordRepository inviteRecordRepository, UserRepository userRepository) {
        this.inviteRecordRepository = inviteRecordRepository;
        this.userRepository = userRepository;
    }

    public User validateInviteCodeForRegistration(String inviteCode) {
        if (inviteCode == null || inviteCode.isBlank()) {
            throw new IllegalArgumentException("邀请码不能为空");
        }
        User inviter = userRepository.findByInviteCode(inviteCode.trim().toUpperCase());
        if (inviter == null) {
            throw new IllegalArgumentException("邀请码无效");
        }
        if ("DISABLED".equalsIgnoreCase(inviter.getUserStatus())) {
            throw new IllegalArgumentException("邀请码无效");
        }
        if ("DISABLED".equalsIgnoreCase(inviter.getInviteCodeStatus())) {
            throw new IllegalArgumentException("邀请码无效");
        }
        return inviter;
    }

    public String generateUniqueInviteCode(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId不能为空");
        }
        String prefix = "LC" + userId;
        for (int i = 0; i < 20; i++) {
            String candidate = prefix + randomSuffix(4);
            if (!userRepository.existsByInviteCode(candidate)) {
                return candidate;
            }
        }
        String fallback = prefix + randomSuffix(6);
        if (!userRepository.existsByInviteCode(fallback)) {
            return fallback;
        }
        throw new IllegalStateException("邀请码生成失败，请稍后重试");
    }

    @Transactional
    public String ensureUserInviteCode(User user) {
        if (user.getInviteCode() != null && !user.getInviteCode().isBlank()) {
            return user.getInviteCode();
        }
        String code = generateUniqueInviteCode(user.getUserid());
        user.setInviteCode(code);
        if (user.getInviteCodeStatus() == null || user.getInviteCodeStatus().isBlank()) {
            user.setInviteCodeStatus("ENABLED");
        }
        userRepository.save(user);
        return code;
    }

    @Transactional
    public void createSuccessRecord(String inviteCode, User inviter, User invitee, String registerIp, String userAgent) {
        InviteRecord record = new InviteRecord();
        record.setInviteCode(inviteCode);
        record.setInviterUserId(inviter.getUserid());
        record.setInviteeUserId(invitee.getUserid());
        record.setInviteeUsername(resolveDisplayName(invitee));
        record.setRegisterIp(trim(registerIp, 64));
        record.setRegisterUserAgent(trim(userAgent, 500));
        record.setStatus("SUCCESS");
        inviteRecordRepository.save(record);
    }

    public Map<String, Object> getMyCodeSummary(User user) {
        String code = ensureUserInviteCode(user);
        long inviteCount = inviteRecordRepository.countByInviterUserIdAndStatus(user.getUserid(), "SUCCESS");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("inviteCode", code);
        result.put("inviteCount", inviteCount);
        return result;
    }

    public List<Map<String, Object>> getMyInvitees(User user) {
        List<InviteRecord> records = inviteRecordRepository
                .findByInviterUserIdAndStatusOrderByCreatedAtDesc(user.getUserid(), "SUCCESS");

        Set<Long> inviteeIds = records.stream()
                .map(InviteRecord::getInviteeUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findAllById(inviteeIds).stream()
                .collect(Collectors.toMap(User::getUserid, item -> item));

        List<Map<String, Object>> result = new ArrayList<>();
        for (InviteRecord record : records) {
            User invitee = userMap.get(record.getInviteeUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", record.getInviteeUserId());
            item.put("nickname", invitee != null ? resolveDisplayName(invitee) : record.getInviteeUsername());
            item.put("username", invitee != null ? safe(invitee.getUsername()) : safe(record.getInviteeUsername()));
            item.put("registeredAt", record.getCreatedAt());
            item.put("status", invitee != null ? normalizeStatus(invitee.getUserStatus()) : "NORMAL");
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> searchInvites(
            Long inviterUserId,
            Long inviteeUserId,
            String inviteCode,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String status
    ) {
        Specification<InviteRecord> specification = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (inviterUserId != null) {
                predicates.add(cb.equal(root.get("inviterUserId"), inviterUserId));
            }
            if (inviteeUserId != null) {
                predicates.add(cb.equal(root.get("inviteeUserId"), inviteeUserId));
            }
            if (inviteCode != null && !inviteCode.isBlank()) {
                predicates.add(cb.equal(root.get("inviteCode"), inviteCode.trim().toUpperCase()));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim().toUpperCase()));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endTime));
            }
            query.orderBy(cb.desc(root.get("createdAt")));
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        List<InviteRecord> records = inviteRecordRepository.findAll(specification);
        Set<Long> userIds = new HashSet<>();
        records.forEach(record -> {
            if (record.getInviterUserId() != null) {
                userIds.add(record.getInviterUserId());
            }
            if (record.getInviteeUserId() != null) {
                userIds.add(record.getInviteeUserId());
            }
        });
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, item -> item));

        List<Map<String, Object>> result = new ArrayList<>();
        for (InviteRecord record : records) {
            User inviter = userMap.get(record.getInviterUserId());
            User invitee = userMap.get(record.getInviteeUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", record.getId());
            item.put("inviteCode", record.getInviteCode());
            item.put("inviterUserId", record.getInviterUserId());
            item.put("inviterName", inviter == null ? "" : resolveDisplayName(inviter));
            item.put("inviteeUserId", record.getInviteeUserId());
            item.put("inviteeName", invitee == null ? safe(record.getInviteeUsername()) : resolveDisplayName(invitee));
            item.put("registerIp", safe(record.getRegisterIp()));
            item.put("registerUserAgent", safe(record.getRegisterUserAgent()));
            item.put("status", record.getStatus());
            item.put("createdAt", record.getCreatedAt());
            result.add(item);
        }
        return result;
    }

    private String randomSuffix(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(RANDOM_CHARS.charAt(RANDOM.nextInt(RANDOM_CHARS.length())));
        }
        return builder.toString();
    }

    private String resolveDisplayName(User user) {
        if (user == null) {
            return "";
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
            return user.getPhoneNumber();
        }
        return "用户" + user.getUserid();
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "NORMAL";
        }
        return status.toUpperCase();
    }

    private String trim(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        String val = text.trim();
        if (val.length() <= maxLength) {
            return val;
        }
        return val.substring(0, maxLength);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}

