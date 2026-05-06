package com.lovecube.backend.services;

import com.lovecube.backend.models.User;
import com.lovecube.backend.models.UserBlacklist;
import com.lovecube.backend.repository.UserBlacklistRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BlacklistService {

    private final UserBlacklistRepository blacklistRepository;
    private final UserRepository userRepository;

    public BlacklistService(UserBlacklistRepository blacklistRepository, UserRepository userRepository) {
        this.blacklistRepository = blacklistRepository;
        this.userRepository = userRepository;
    }

    public Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("未提供或格式错误的 token");
        }
        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) throw new IllegalArgumentException("token 无效");
        User user = userRepository.findByOpenid(openid);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        return user.getUserid();
    }

    public List<Map<String, Object>> getMyBlacklist(Long userId) {
        return blacklistRepository.findByUserId(userId).stream()
            .map(b -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", b.getId());
                item.put("blockedUserId", b.getBlockedUserId());
                item.put("reason", b.getReason());
                item.put("createdAt", b.getCreatedAt());
                userRepository.findById(b.getBlockedUserId()).ifPresent(u -> {
                    item.put("nickname", u.getUsername());
                    item.put("avatarUrl", u.getProfilePhoto());
                });
                return item;
            })
            .collect(Collectors.toList());
    }

    public void blockUser(Long userId, Long targetId, String reason) {
        if (userId.equals(targetId)) throw new IllegalArgumentException("不能拉黑自己");
        if (blacklistRepository.existsByUserIdAndBlockedUserId(userId, targetId)) return;
        UserBlacklist entry = new UserBlacklist();
        entry.setUserId(userId);
        entry.setBlockedUserId(targetId);
        entry.setReason(reason);
        blacklistRepository.save(entry);
    }

    public void unblockUser(Long userId, Long targetId) {
        blacklistRepository.findByUserIdAndBlockedUserId(userId, targetId)
            .ifPresent(blacklistRepository::delete);
    }

    public Map<String, Object> getBlockStatus(Long myId, Long targetId) {
        boolean blockedByMe = blacklistRepository.existsByUserIdAndBlockedUserId(myId, targetId);
        boolean blockedMe   = blacklistRepository.existsByUserIdAndBlockedUserId(targetId, myId);
        Map<String, Object> result = new HashMap<>();
        result.put("blockedByMe", blockedByMe);
        result.put("blockedMe",   blockedMe);
        result.put("canMessage",  !blockedByMe && !blockedMe);
        return result;
    }

    public boolean isBlocked(Long senderId, Long receiverId) {
        return blacklistRepository.existsByUserIdAndBlockedUserId(senderId, receiverId)
            || blacklistRepository.existsByUserIdAndBlockedUserId(receiverId, senderId);
    }

    public List<Long> getBlockedAndBlockerIds(Long userId) {
        List<Long> blocked  = blacklistRepository.findBlockedUserIdsByUserId(userId);
        List<Long> blockers = blacklistRepository.findBlockerUserIdsByUserId(userId);
        Set<Long> all = new HashSet<>(blocked);
        all.addAll(blockers);
        return new ArrayList<>(all);
    }
}
