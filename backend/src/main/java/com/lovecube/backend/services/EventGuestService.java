package com.lovecube.backend.services;

import com.lovecube.backend.dating.DatingEventTemplate;
import com.lovecube.backend.entity.EventGuestParticipant;
import com.lovecube.backend.entity.EventSignup;
import com.lovecube.backend.entity.PlatformEvent;
import com.lovecube.backend.repository.EventGuestParticipantRepository;
import com.lovecube.backend.repository.EventSignupRepository;
import com.lovecube.backend.repository.PlatformEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class EventGuestService {

    private static final int GUEST_TOKEN_EXPIRE_DAYS = 14;

    private final PlatformEventRepository eventRepository;
    private final EventGuestParticipantRepository guestRepository;
    private final EventSignupRepository signupRepository;
    private final EventSignupService eventSignupService;
    private final EventEngagementService eventEngagementService;
    private final DatingEventService datingEventService;

    public EventGuestService(
            PlatformEventRepository eventRepository,
            EventGuestParticipantRepository guestRepository,
            EventSignupRepository signupRepository,
            EventSignupService eventSignupService,
            EventEngagementService eventEngagementService,
            DatingEventService datingEventService
    ) {
        this.eventRepository = eventRepository;
        this.guestRepository = guestRepository;
        this.signupRepository = signupRepository;
        this.eventSignupService = eventSignupService;
        this.eventEngagementService = eventEngagementService;
        this.datingEventService = datingEventService;
    }

    public EventGuestParticipant requireValidGuest(String eventId, String guestToken) {
        if (guestToken == null || guestToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "缺少游客凭证");
        }
        EventGuestParticipant guest = guestRepository.findByEventIdAndGuestToken(eventId, guestToken.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "游客身份无效或已过期"));
        if (guest.getExpiresAt() != null && guest.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "游客身份已过期，请重新参加本场活动");
        }
        return guest;
    }

    @Transactional
    public Map<String, Object> createOrRestoreSession(String eventId, Map<String, Object> body, String existingToken) {
        PlatformEvent event = eventSignupService.requirePublishedEvent(eventId);
        if (!datingEventService.isDatingEvent(event)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前活动暂不支持游客快速参加");
        }

        if (existingToken != null && !existingToken.isBlank()) {
            Optional<EventGuestParticipant> existing = guestRepository.findByEventIdAndGuestToken(eventId, existingToken.trim());
            if (existing.isPresent() && !isExpired(existing.get())) {
                return toSessionMap(existing.get());
            }
        }

        String nickname = trim(body != null ? body.get("nickname") : null, 64);
        String genderSide = normalizeGenderSide(body != null ? body.get("genderSide") : null);
        if (nickname == null || nickname.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请填写昵称");
        }
        if (genderSide == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择性别");
        }

        EventGuestParticipant guest = new EventGuestParticipant();
        guest.setEventId(eventId);
        guest.setGuestToken(generateGuestToken());
        guest.setNickname(nickname);
        guest.setGenderSide(genderSide);
        applyMobile(guest, body != null ? body.get("mobile") : null);
        guest.setExpiresAt(resolveExpiresAt(event));
        guestRepository.save(guest);
        return toSessionMap(guest);
    }

    public Map<String, Object> getContext(String eventId, String guestToken) {
        EventGuestParticipant guest = requireValidGuest(eventId, guestToken);
        PlatformEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));

        Optional<EventSignup> signup = signupRepository.findByEventIdAndGuestParticipantId(eventId, guest.getId());
        boolean registered = signup.isPresent();
        boolean checkedIn = signup.map(s -> Boolean.TRUE.equals(s.getCheckedIn())).orElse(false);

        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("eventId", eventId);
        ctx.put("title", event.getTitle());
        ctx.put("templateType", event.getTemplateType());
        ctx.put("location", event.getLocation() == null ? "" : event.getLocation());
        ctx.put("eventTime", event.getEventTime());
        ctx.put("guestParticipantId", guest.getId());
        ctx.put("guestToken", guest.getGuestToken());
        ctx.put("nickname", guest.getNickname());
        ctx.put("genderSide", guest.getGenderSide());
        ctx.put("registered", registered);
        ctx.put("checkedIn", checkedIn);
        ctx.put("eventStatus", resolveEventStatusKey(event, registered));
        ctx.put("canJoin", !registered && canJoinEvent(event));
        ctx.put("canCheckin", registered && !checkedIn && hasCheckinCode(event) && !PlatformEventLifecycle.isEnded(event, LocalDateTime.now()));
        ctx.put("checkinRequired", registered && !checkedIn && hasCheckinCode(event));

        datingEventService.findIdentityForGuest(eventId, guest.getId())
                .ifPresent(identity -> ctx.put("badgeLabel", identity.getBadgeLabel()));

        boolean profileCompleted = datingEventService.findProfileForGuest(eventId, guest.getId())
                .map(p -> Boolean.TRUE.equals(p.getCompleted()))
                .orElse(false);
        ctx.put("profileCompleted", profileCompleted);
        ctx.put("canEnterDating", checkedIn);
        ctx.put("targetUrl", checkedIn ? "/fellowship/events/" + eventId + "/dating" : "/events/" + eventId + "/onsite");
        ctx.put("statusMessage", resolveStatusMessage(ctx));
        return ctx;
    }

    @Transactional
    public Map<String, Object> join(String eventId, String guestToken) {
        EventGuestParticipant guest = requireValidGuest(eventId, guestToken);
        return eventSignupService.signupGuest(guest);
    }

    @Transactional
    public Map<String, Object> checkin(String eventId, String guestToken, String code) {
        EventGuestParticipant guest = requireValidGuest(eventId, guestToken);
        PlatformEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        Map<String, Object> result = new LinkedHashMap<>(eventEngagementService.checkinGuest(guest, eventId, code));
        result.put("targetUrl", datingEventService.resolveTargetUrl(event, true));
        result.put("guestToken", guest.getGuestToken());
        return result;
    }

    private Map<String, Object> toSessionMap(EventGuestParticipant guest) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("guestToken", guest.getGuestToken());
        map.put("guestParticipantId", guest.getId());
        map.put("nickname", guest.getNickname());
        map.put("genderSide", guest.getGenderSide());
        map.put("eventId", guest.getEventId());
        return map;
    }

    private boolean isExpired(EventGuestParticipant guest) {
        return guest.getExpiresAt() != null && guest.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private LocalDateTime resolveExpiresAt(PlatformEvent event) {
        LocalDateTime end = PlatformEventLifecycle.resolveEnd(event);
        if (end != null) {
            return end.plusDays(GUEST_TOKEN_EXPIRE_DAYS);
        }
        return LocalDateTime.now().plusDays(GUEST_TOKEN_EXPIRE_DAYS);
    }

    private void applyMobile(EventGuestParticipant guest, Object mobileRaw) {
        if (mobileRaw == null) {
            return;
        }
        String mobile = String.valueOf(mobileRaw).replaceAll("\\s+", "");
        if (mobile.isBlank()) {
            return;
        }
        if (mobile.length() >= 4) {
            guest.setMobileLast4(mobile.substring(mobile.length() - 4));
        }
        guest.setMobileHash(sha256(mobile));
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return null;
        }
    }

    private String generateGuestToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String normalizeGenderSide(Object raw) {
        if (raw == null) {
            return null;
        }
        String v = String.valueOf(raw).trim().toUpperCase();
        if (DatingEventTemplate.SIDE_MALE.equals(v) || "男".equals(String.valueOf(raw).trim()) || "M".equals(v)) {
            return DatingEventTemplate.SIDE_MALE;
        }
        if (DatingEventTemplate.SIDE_FEMALE.equals(v) || "女".equals(String.valueOf(raw).trim()) || "F".equals(v)) {
            return DatingEventTemplate.SIDE_FEMALE;
        }
        if (DatingEventTemplate.SIDE_OTHER.equals(v) || "其他".equals(String.valueOf(raw).trim()) || "O".equals(v)) {
            return DatingEventTemplate.SIDE_OTHER;
        }
        return null;
    }

    private String trim(Object raw, int max) {
        if (raw == null) {
            return null;
        }
        String s = String.valueOf(raw).trim();
        if (s.isEmpty()) {
            return null;
        }
        return s.length() > max ? s.substring(0, max) : s;
    }

    private boolean hasCheckinCode(PlatformEvent event) {
        return event.getCheckinCode() != null && !event.getCheckinCode().isBlank();
    }

    private boolean canJoinEvent(PlatformEvent event) {
        if (!"published".equalsIgnoreCase(event.getStatus())) {
            return false;
        }
        if (PlatformEventLifecycle.isEnded(event, LocalDateTime.now())) {
            return false;
        }
        return !eventSignupService.isFull(event);
    }

    private String resolveEventStatusKey(PlatformEvent event, boolean registered) {
        if (!"published".equalsIgnoreCase(event.getStatus())) {
            return "CLOSED";
        }
        if (PlatformEventLifecycle.isEnded(event, LocalDateTime.now())) {
            return "ENDED";
        }
        if (!registered && eventSignupService.isFull(event)) {
            return "FULL";
        }
        return "OPEN";
    }

    private String resolveStatusMessage(Map<String, Object> ctx) {
        String eventStatus = String.valueOf(ctx.get("eventStatus"));
        if ("CLOSED".equals(eventStatus)) {
            return "活动未开放";
        }
        if ("ENDED".equals(eventStatus)) {
            return "活动已结束";
        }
        if ("FULL".equals(eventStatus) && !Boolean.TRUE.equals(ctx.get("registered"))) {
            return "名额已满";
        }
        if (Boolean.TRUE.equals(ctx.get("checkedIn"))) {
            return "你已完成签到";
        }
        if (Boolean.TRUE.equals(ctx.get("canCheckin"))) {
            return "请输入现场签到码";
        }
        if (Boolean.TRUE.equals(ctx.get("canJoin"))) {
            return "确认参加本场活动";
        }
        if (Boolean.TRUE.equals(ctx.get("registered"))) {
            return "你已加入本场活动";
        }
        return "";
    }
}
