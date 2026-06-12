package com.lovecube.backend.services;

import com.lovecube.backend.dating.DatingEventTemplate;
import com.lovecube.backend.entity.EventSignup;
import com.lovecube.backend.entity.PlatformEvent;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.EventSignupRepository;
import com.lovecube.backend.repository.PlatformEventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ActivityOnsiteEntryService {

    private final PlatformEventRepository eventRepository;
    private final EventSignupRepository signupRepository;
    private final EventSignupService eventSignupService;
    private final EventEngagementService eventEngagementService;
    private final DatingEventService datingEventService;

    public ActivityOnsiteEntryService(
            PlatformEventRepository eventRepository,
            EventSignupRepository signupRepository,
            EventSignupService eventSignupService,
            EventEngagementService eventEngagementService,
            DatingEventService datingEventService
    ) {
        this.eventRepository = eventRepository;
        this.signupRepository = signupRepository;
        this.eventSignupService = eventSignupService;
        this.eventEngagementService = eventEngagementService;
        this.datingEventService = datingEventService;
    }

    public Map<String, Object> getContext(String eventId, User user) {
        PlatformEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));

        LocalDateTime now = LocalDateTime.now();
        boolean ended = PlatformEventLifecycle.isEnded(event, now);
        boolean published = "published".equalsIgnoreCase(event.getStatus());
        boolean full = eventSignupService.isFull(event);

        String eventStatus;
        if (!published) {
            eventStatus = "CLOSED";
        } else if (ended) {
            eventStatus = "ENDED";
        } else if (full) {
            eventStatus = "FULL";
        } else {
            eventStatus = "OPEN";
        }

        boolean loggedIn = user != null;
        boolean registered = false;
        boolean checkedIn = false;
        if (loggedIn) {
            Optional<EventSignup> signup = signupRepository.findByEventIdAndUserId(eventId, user.getUserid());
            registered = signup.isPresent();
            checkedIn = signup.map(s -> Boolean.TRUE.equals(s.getCheckedIn())).orElse(false);
        }

        boolean checkinEnabled = event.getCheckinCode() != null && !event.getCheckinCode().isBlank();
        boolean canJoin = loggedIn && published && !ended && !full && !registered;
        boolean canCheckin = loggedIn && registered && !checkedIn && checkinEnabled && !ended;
        boolean checkinRequired = registered && !checkedIn && checkinEnabled && !ended;

        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("eventId", event.getId());
        ctx.put("title", event.getTitle());
        ctx.put("templateType", event.getTemplateType() == null ? DatingEventTemplate.TYPE_GENERAL : event.getTemplateType());
        ctx.put("eventStatus", eventStatus);
        ctx.put("location", event.getLocation() == null ? "" : event.getLocation());
        ctx.put("eventTime", event.getEventTime());
        ctx.put("loggedIn", loggedIn);
        ctx.put("registered", registered);
        ctx.put("checkedIn", checkedIn);
        ctx.put("canJoin", canJoin);
        ctx.put("canCheckin", canCheckin);
        ctx.put("checkinRequired", checkinRequired);
        ctx.put("targetUrl", resolveTargetUrl(event, checkedIn));

        if (loggedIn && checkedIn) {
            datingEventService.findIdentity(eventId, user.getUserid())
                    .ifPresent(identity -> ctx.put("badgeLabel", identity.getBadgeLabel()));
        }

        ctx.put("statusMessage", resolveStatusMessage(eventStatus, loggedIn, registered, checkedIn, canJoin, canCheckin));
        return ctx;
    }

    @Transactional
    public Map<String, Object> join(User user, String eventId) {
        Map<String, Object> signupResult = eventSignupService.signup(user, eventId);
        PlatformEvent event = eventRepository.findById(eventId).orElseThrow();
        signupResult.put("targetUrl", "/events/" + eventId + "/onsite");
        signupResult.put("eventStatus", resolveEventStatusKey(event));
        signupResult.put("canCheckin", canCheckinForUser(event, user.getUserid()));
        return signupResult;
    }

    @Transactional
    public Map<String, Object> checkin(User user, String eventId, String code) {
        PlatformEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        Map<String, Object> result = new LinkedHashMap<>(eventEngagementService.checkin(user, eventId, code));
        result.put("targetUrl", resolveTargetUrl(event, true));
        result.put("templateType", event.getTemplateType());
        if (datingEventService.isDatingEvent(event)) {
            Object datingIdentity = result.get("datingIdentity");
            if (datingIdentity == null) {
                try {
                    result.put("datingIdentity", datingEventService.ensureIdentityAfterCheckin(user, eventId));
                } catch (Exception ignored) {
                }
            }
        }
        return result;
    }

    private boolean canCheckinForUser(PlatformEvent event, Long userId) {
        if (PlatformEventLifecycle.isEnded(event, LocalDateTime.now())) {
            return false;
        }
        EventSignup signup = signupRepository.findByEventIdAndUserId(event.getId(), userId).orElse(null);
        if (signup == null || Boolean.TRUE.equals(signup.getCheckedIn())) {
            return false;
        }
        return event.getCheckinCode() != null && !event.getCheckinCode().isBlank();
    }

    private String resolveEventStatusKey(PlatformEvent event) {
        if (!"published".equalsIgnoreCase(event.getStatus())) {
            return "CLOSED";
        }
        if (PlatformEventLifecycle.isEnded(event, LocalDateTime.now())) {
            return "ENDED";
        }
        if (eventSignupService.isFull(event)) {
            return "FULL";
        }
        return "OPEN";
    }

    public String resolveTargetUrl(PlatformEvent event, boolean checkedIn) {
        if (!checkedIn) {
            return "/events/" + event.getId() + "/onsite";
        }
        if (DatingEventTemplate.isDating(event.getTemplateType())) {
            return "/fellowship/events/" + event.getId() + "/dating";
        }
        return "/events/" + event.getId();
    }

    private String resolveStatusMessage(
            String eventStatus,
            boolean loggedIn,
            boolean registered,
            boolean checkedIn,
            boolean canJoin,
            boolean canCheckin
    ) {
        if ("CLOSED".equals(eventStatus)) {
            return "活动未开放";
        }
        if ("ENDED".equals(eventStatus)) {
            return "活动已结束";
        }
        if ("FULL".equals(eventStatus) && !registered) {
            return "名额已满";
        }
        if (!loggedIn) {
            return "请先登录后加入活动";
        }
        if (checkedIn) {
            return "你已完成签到";
        }
        if (canCheckin) {
            return "请输入现场签到码";
        }
        if (canJoin) {
            return "确认参加本场活动";
        }
        if (registered) {
            return "你已加入本场活动";
        }
        return "";
    }
}
