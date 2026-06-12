package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.ActivityOnsiteEntryService;
import com.lovecube.backend.services.AdminAuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/events/{eventId}/onsite")
public class ActivityOnsiteEntryController {

    private final AdminAuthService adminAuthService;
    private final ActivityOnsiteEntryService onsiteEntryService;

    public ActivityOnsiteEntryController(
            AdminAuthService adminAuthService,
            ActivityOnsiteEntryService onsiteEntryService
    ) {
        this.adminAuthService = adminAuthService;
        this.onsiteEntryService = onsiteEntryService;
    }

    @GetMapping("/context")
    public Map<String, Object> getContext(
            @PathVariable String eventId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = tryCurrentUser(authHeader);
        return onsiteEntryService.getContext(eventId, user);
    }

    @PostMapping("/join")
    public Map<String, Object> join(
            @PathVariable String eventId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return onsiteEntryService.join(user, eventId);
    }

    @PostMapping("/checkin")
    public Map<String, Object> checkin(
            @PathVariable String eventId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        String code = body != null ? String.valueOf(body.getOrDefault("code", "")) : "";
        return onsiteEntryService.checkin(user, eventId, code);
    }

    private User tryCurrentUser(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }
        try {
            return adminAuthService.requireUser(authHeader);
        } catch (Exception e) {
            return null;
        }
    }
}
