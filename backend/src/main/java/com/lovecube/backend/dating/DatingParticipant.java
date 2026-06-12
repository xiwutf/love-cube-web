package com.lovecube.backend.dating;

import com.lovecube.backend.entity.EventGuestParticipant;
import com.lovecube.backend.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public record DatingParticipant(
        String eventId,
        Long userId,
        Long guestParticipantId,
        String genderSide,
        String nickname
) {

    public static final String TYPE_REGISTERED = "REGISTERED";
    public static final String TYPE_GUEST = "GUEST";

    public static DatingParticipant fromUser(User user, String eventId) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        }
        return new DatingParticipant(eventId, user.getUserid(), null, null, user.getUsername());
    }

    public static DatingParticipant fromGuest(EventGuestParticipant guest) {
        if (guest == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "游客身份无效");
        }
        return new DatingParticipant(
                guest.getEventId(),
                null,
                guest.getId(),
                guest.getGenderSide(),
                guest.getNickname()
        );
    }

    public boolean isGuest() {
        return guestParticipantId != null;
    }

    public String participantType() {
        return isGuest() ? TYPE_GUEST : TYPE_REGISTERED;
    }

    public String participantKey() {
        if (isGuest()) {
            return TYPE_GUEST + ":" + guestParticipantId;
        }
        return TYPE_REGISTERED + ":" + userId;
    }

    public static ParsedParticipantKey parseKey(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参与者标识无效");
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith(TYPE_REGISTERED + ":")) {
            try {
                long id = Long.parseLong(trimmed.substring((TYPE_REGISTERED + ":").length()));
                return new ParsedParticipantKey(TYPE_REGISTERED, id);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参与者标识无效");
            }
        }
        if (trimmed.startsWith(TYPE_GUEST + ":")) {
            try {
                long id = Long.parseLong(trimmed.substring((TYPE_GUEST + ":").length()));
                return new ParsedParticipantKey(TYPE_GUEST, id);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参与者标识无效");
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参与者标识无效");
    }

    public record ParsedParticipantKey(String type, long id) {
        public boolean isGuest() {
            return TYPE_GUEST.equals(type);
        }
    }
}
