package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.SiteVisitLog;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.SiteVisitLogRepository;
import com.lovecube.backend.services.AdminAuthService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final SiteVisitLogRepository siteVisitLogRepository;
    private final AdminAuthService adminAuthService;

    public AnalyticsController(SiteVisitLogRepository siteVisitLogRepository, AdminAuthService adminAuthService) {
        this.siteVisitLogRepository = siteVisitLogRepository;
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/track")
    public Map<String, Object> trackVisit(
            @RequestBody Map<String, Object> payload,
            HttpServletRequest request
    ) {
        String visitorId = toText(payload.get("visitorId"));
        if (visitorId == null || visitorId.isBlank()) {
            return Map.of("ok", false, "message", "visitorId is required");
        }

        String path = toText(payload.get("path"));
        if (path == null || path.isBlank()) {
            path = "/";
        }

        String userAgent = request.getHeader("User-Agent");
        SiteVisitLog log = new SiteVisitLog();
        log.setVisitorId(visitorId);
        log.setUserId(resolveCurrentUserId(request.getHeader("Authorization")));
        log.setPath(path);
        log.setReferrer(toText(payload.get("referrer")));
        log.setIpAddress(resolveClientIp(request));
        log.setUserAgent(userAgent);
        log.setDeviceType(resolveDeviceType(userAgent));
        log.setBrowser(resolveBrowser(userAgent));
        log.setOs(resolveOs(userAgent));
        siteVisitLogRepository.save(log);
        return Map.of("ok", true);
    }

    private Long resolveCurrentUserId(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }
        try {
            User user = adminAuthService.requireUser(authHeader);
            return user == null ? null : user.getUserid();
        } catch (Exception ignore) {
            return null;
        }
    }

    private String toText(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] chunks = forwardedFor.split(",");
            if (chunks.length > 0) {
                return chunks[0].trim();
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveDeviceType(String userAgent) {
        String ua = userAgent == null ? "" : userAgent.toLowerCase();
        if (ua.contains("ipad") || ua.contains("tablet")) return "tablet";
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) return "mobile";
        return "desktop";
    }

    private String resolveBrowser(String userAgent) {
        String ua = userAgent == null ? "" : userAgent.toLowerCase();
        if (ua.contains("edg/")) return "edge";
        if (ua.contains("firefox/")) return "firefox";
        if (ua.contains("chrome/")) return "chrome";
        if (ua.contains("safari/") && !ua.contains("chrome/")) return "safari";
        if (ua.contains("micromessenger")) return "wechat";
        return "other";
    }

    private String resolveOs(String userAgent) {
        String ua = userAgent == null ? "" : userAgent.toLowerCase();
        if (ua.contains("windows")) return "windows";
        if (ua.contains("android")) return "android";
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ios")) return "ios";
        if (ua.contains("mac os")) return "macos";
        if (ua.contains("linux")) return "linux";
        return "other";
    }
}
