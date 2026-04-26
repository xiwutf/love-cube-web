package com.lovecube.backend.services;

import com.lovecube.backend.models.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FellowshipProfileService {
    private final UnifiedProfileService unifiedProfileService;

    public FellowshipProfileService(UnifiedProfileService unifiedProfileService) {
        this.unifiedProfileService = unifiedProfileService;
    }

    public User requireCurrentUser(String authHeader) {
        return unifiedProfileService.requireCurrentUser(authHeader);
    }

    public Map<String, Object> getMyProfile(User user) {
        return unifiedProfileService.buildFellowshipPayload(user);
    }

    public Map<String, Object> updateMyProfile(User user, Map<String, Object> payload) {
        return unifiedProfileService.updateFellowshipProfile(user, payload);
    }

    public Map<String, Object> getCompletion(User user) {
        return unifiedProfileService.buildFellowshipCompletion(user);
    }

    public Map<String, Object> toResponse(Map<String, Object> profile) {
        return profile;
    }

    public List<Map<String, Object>> listGuardianProfiles() {
        return List.of();
    }
}
