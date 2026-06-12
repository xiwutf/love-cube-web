package com.lovecube.backend.controllers;

import com.lovecube.backend.services.EventGuestService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/events/{eventId}/guest")
public class EventGuestController {

    private final EventGuestService eventGuestService;

    public EventGuestController(EventGuestService eventGuestService) {
        this.eventGuestService = eventGuestService;
    }

    @PostMapping("/session")
    public Map<String, Object> createSession(
            @PathVariable String eventId,
            @RequestBody(required = false) Map<String, Object> body,
            @RequestHeader(value = "X-Event-Guest-Token", required = false) String guestToken
    ) {
        return eventGuestService.createOrRestoreSession(eventId, body, guestToken);
    }

    @GetMapping("/context")
    public Map<String, Object> getContext(
            @PathVariable String eventId,
            @RequestHeader(value = "X-Event-Guest-Token", required = false) String headerToken,
            @RequestParam(value = "guestToken", required = false) String queryToken
    ) {
        String token = headerToken != null && !headerToken.isBlank() ? headerToken : queryToken;
        return eventGuestService.getContext(eventId, token);
    }

    @PostMapping("/join")
    public Map<String, Object> join(
            @PathVariable String eventId,
            @RequestHeader(value = "X-Event-Guest-Token", required = false) String headerToken,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        String token = resolveGuestToken(headerToken, body);
        return eventGuestService.join(eventId, token);
    }

    @PostMapping("/checkin")
    public Map<String, Object> checkin(
            @PathVariable String eventId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-Event-Guest-Token", required = false) String headerToken
    ) {
        String token = resolveGuestToken(headerToken, body);
        String code = body != null ? String.valueOf(body.getOrDefault("code", "")) : "";
        return eventGuestService.checkin(eventId, token, code);
    }

    private String resolveGuestToken(String headerToken, Map<String, Object> body) {
        if (headerToken != null && !headerToken.isBlank()) {
            return headerToken.trim();
        }
        if (body != null && body.get("guestToken") != null) {
            return String.valueOf(body.get("guestToken")).trim();
        }
        return "";
    }
}
