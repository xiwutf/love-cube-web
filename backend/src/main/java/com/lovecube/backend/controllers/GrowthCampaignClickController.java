package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.GrowthCampaignService;
import com.lovecube.backend.services.UnifiedProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/growth/campaign")
public class GrowthCampaignClickController {
    private final UnifiedProfileService unifiedProfileService;
    private final GrowthCampaignService growthCampaignService;

    public GrowthCampaignClickController(
            UnifiedProfileService unifiedProfileService,
            GrowthCampaignService growthCampaignService
    ) {
        this.unifiedProfileService = unifiedProfileService;
        this.growthCampaignService = growthCampaignService;
    }

    @PostMapping("/delivery/{deliveryId}/click")
    public ResponseEntity<?> recordClick(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long deliveryId,
            HttpServletRequest request
    ) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            Map<String, Object> result = growthCampaignService.recordDeliveryClick(
                    user,
                    deliveryId,
                    request.getHeader("User-Agent"),
                    resolveClientIp(request)
            );
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    private static String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int comma = forwardedFor.indexOf(',');
            return comma > 0 ? forwardedFor.substring(0, comma).trim() : forwardedFor.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
