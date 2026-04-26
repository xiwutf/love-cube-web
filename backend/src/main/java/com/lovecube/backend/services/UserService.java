package com.lovecube.backend.services;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UnifiedProfileService unifiedProfileService;

    public UserService(UserRepository userRepository, UnifiedProfileService unifiedProfileService) {
        this.userRepository = userRepository;
        this.unifiedProfileService = unifiedProfileService;
    }

    public Map<String, Object> getUserProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        return unifiedProfileService.buildLegacyUserPayload(user);
    }

    @Transactional
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getUserid())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (user.getUsername() != null) existingUser.setUsername(user.getUsername());
        if (user.getGender() != null) existingUser.setGender(user.getGender());
        if (user.getLocation() != null) existingUser.setLocation(user.getLocation());
        if (user.getOccupation() != null) existingUser.setOccupation(user.getOccupation());
        if (user.getHeight() != null) existingUser.setHeight(user.getHeight());
        if (user.getProfilePhoto() != null) existingUser.setProfilePhoto(user.getProfilePhoto());
        if (user.getBio() != null) existingUser.setBio(user.getBio());
        if (user.getAge() != null) existingUser.setAge(user.getAge());
        return userRepository.save(existingUser);
    }

    public User getCurrentUser(String token) {
        String openid = JwtUtil.getOpenIdFromToken(token.replace("Bearer ", ""));
        return userRepository.findByOpenid(openid);
    }
}
