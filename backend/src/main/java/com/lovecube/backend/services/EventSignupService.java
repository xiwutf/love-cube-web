package com.lovecube.backend.services;

import com.lovecube.backend.entity.EventGuestParticipant;
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

@Service
public class EventSignupService {

    private final PlatformEventRepository eventRepository;
    private final EventSignupRepository signupRepository;

    public EventSignupService(
            PlatformEventRepository eventRepository,
            EventSignupRepository signupRepository
    ) {
        this.eventRepository = eventRepository;
        this.signupRepository = signupRepository;
    }

    public PlatformEvent requirePublishedEvent(String eventId) {
        PlatformEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        if (!"published".equalsIgnoreCase(event.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动未开放");
        }
        return event;
    }

    public void assertJoinable(PlatformEvent event) {
        if (PlatformEventLifecycle.isEnded(event, LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动已结束");
        }
        if (isFull(event)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "名额已满");
        }
    }

    public boolean isRegistered(String eventId, Long userId) {
        return signupRepository.existsByEventIdAndUserId(eventId, userId);
    }

    public boolean isFull(PlatformEvent event) {
        Integer max = event.getMaxSignupCount();
        if (max == null || max <= 0) {
            return false;
        }
        int count = event.getSignupCount() == null ? 0 : event.getSignupCount();
        return count >= max;
    }

    @Transactional
    public Map<String, Object> signup(User user, String eventId) {
        PlatformEvent item = requirePublishedEvent(eventId);
        boolean alreadySignedUp = signupRepository.existsByEventIdAndUserId(eventId, user.getUserid());
        if (!alreadySignedUp) {
            assertJoinable(item);
            EventSignup signup = new EventSignup();
            signup.setEventId(eventId);
            signup.setUserId(user.getUserid());
            signupRepository.save(signup);
            item.setSignupCount((item.getSignupCount() == null ? 0 : item.getSignupCount()) + 1);
            eventRepository.save(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("signedUp", true);
        result.put("registered", true);
        result.put("alreadySignedUp", alreadySignedUp);
        result.put("signupCount", item.getSignupCount() == null ? 0 : item.getSignupCount());
        result.put("message", alreadySignedUp ? "已报名，无需重复提交" : "报名成功");
        return result;
    }

    @Transactional
    public Map<String, Object> signupGuest(EventGuestParticipant guest) {
        String eventId = guest.getEventId();
        PlatformEvent item = requirePublishedEvent(eventId);
        boolean alreadySignedUp = signupRepository.existsByEventIdAndGuestParticipantId(eventId, guest.getId());
        if (!alreadySignedUp) {
            assertJoinable(item);
            EventSignup signup = new EventSignup();
            signup.setEventId(eventId);
            signup.setGuestParticipantId(guest.getId());
            signupRepository.save(signup);
            item.setSignupCount((item.getSignupCount() == null ? 0 : item.getSignupCount()) + 1);
            eventRepository.save(item);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("signedUp", true);
        result.put("registered", true);
        result.put("alreadySignedUp", alreadySignedUp);
        result.put("guestParticipantId", guest.getId());
        result.put("signupCount", item.getSignupCount() == null ? 0 : item.getSignupCount());
        result.put("message", alreadySignedUp ? "已加入，无需重复提交" : "已加入本场活动");
        return result;
    }
}
