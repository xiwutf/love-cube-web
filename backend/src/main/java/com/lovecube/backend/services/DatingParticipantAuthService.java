package com.lovecube.backend.services;

import com.lovecube.backend.dating.DatingParticipant;
import com.lovecube.backend.entity.EventGuestParticipant;
import com.lovecube.backend.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DatingParticipantAuthService {

    private final AdminAuthService adminAuthService;
    private final EventGuestService eventGuestService;

    public DatingParticipantAuthService(AdminAuthService adminAuthService, EventGuestService eventGuestService) {
        this.adminAuthService = adminAuthService;
        this.eventGuestService = eventGuestService;
    }

    public DatingParticipant resolve(String eventId, String authHeader, String guestToken) {
        User user = tryCurrentUser(authHeader);
        if (user != null) {
            return DatingParticipant.fromUser(user, eventId);
        }
        if (guestToken != null && !guestToken.isBlank()) {
            EventGuestParticipant guest = eventGuestService.requireValidGuest(eventId, guestToken);
            return DatingParticipant.fromGuest(guest);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录或使用本场活动游客身份");
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
