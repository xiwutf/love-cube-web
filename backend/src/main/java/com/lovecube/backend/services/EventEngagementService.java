package com.lovecube.backend.services;

import com.lovecube.backend.entity.EventGuestParticipant;
import com.lovecube.backend.entity.EventPeerReview;
import com.lovecube.backend.entity.EventSignup;
import com.lovecube.backend.entity.PlatformEvent;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.EventPeerReviewRepository;
import com.lovecube.backend.repository.EventSignupRepository;
import com.lovecube.backend.repository.PlatformEventRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventEngagementService {

    private final PlatformEventRepository eventRepository;
    private final EventSignupRepository signupRepository;
    private final EventPeerReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final DatingEventService datingEventService;

    public EventEngagementService(
            PlatformEventRepository eventRepository,
            EventSignupRepository signupRepository,
            EventPeerReviewRepository reviewRepository,
            UserRepository userRepository,
            DatingEventService datingEventService
    ) {
        this.eventRepository = eventRepository;
        this.signupRepository = signupRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.datingEventService = datingEventService;
    }

    @Transactional
    public Map<String, Object> checkin(User user, String eventId, String code) {
        PlatformEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        if (!"published".equals(event.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动暂不可签到");
        }
        String expected = event.getCheckinCode();
        if (expected == null || expected.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动尚未开启签到");
        }
        if (code == null || !expected.equalsIgnoreCase(code.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "签到码不正确");
        }
        EventSignup signup = signupRepository.findByEventIdAndUserId(eventId, user.getUserid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先报名活动"));
        if (Boolean.TRUE.equals(signup.getCheckedIn())) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("checkedIn", true);
            result.put("alreadyCheckedIn", true);
            result.put("checkedInAt", signup.getCheckedInAt() != null ? signup.getCheckedInAt().toString() : null);
            result.put("message", "你已签到，无需重复操作");
            appendDatingIdentity(event, user, eventId, result);
            return result;
        }
        signup.setCheckedIn(true);
        signup.setCheckedInAt(LocalDateTime.now());
        signupRepository.save(signup);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("checkedIn", true);
        result.put("alreadyCheckedIn", false);
        result.put("checkedInAt", signup.getCheckedInAt().toString());
        result.put("message", "签到成功");
        appendDatingIdentity(event, user, eventId, result);
        return result;
    }

    @Transactional
    public Map<String, Object> checkinGuest(EventGuestParticipant guest, String eventId, String code) {
        PlatformEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        if (!"published".equals(event.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动暂不可签到");
        }
        String expected = event.getCheckinCode();
        if (expected == null || expected.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动尚未开启签到");
        }
        if (code == null || !expected.equalsIgnoreCase(code.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "签到码不正确");
        }
        EventSignup signup = signupRepository.findByEventIdAndGuestParticipantId(eventId, guest.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先加入本场活动"));
        if (Boolean.TRUE.equals(signup.getCheckedIn())) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("checkedIn", true);
            result.put("alreadyCheckedIn", true);
            result.put("checkedInAt", signup.getCheckedInAt() != null ? signup.getCheckedInAt().toString() : null);
            result.put("message", "你已签到，无需重复操作");
            appendDatingIdentityForGuest(event, guest, eventId, result);
            return result;
        }
        signup.setCheckedIn(true);
        signup.setCheckedInAt(LocalDateTime.now());
        signupRepository.save(signup);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("checkedIn", true);
        result.put("alreadyCheckedIn", false);
        result.put("checkedInAt", signup.getCheckedInAt().toString());
        result.put("message", "签到成功");
        appendDatingIdentityForGuest(event, guest, eventId, result);
        return result;
    }

    private void appendDatingIdentity(PlatformEvent event, User user, String eventId, Map<String, Object> result) {
        if (!datingEventService.isDatingEvent(event)) {
            return;
        }
        try {
            Map<String, Object> identity = datingEventService.ensureIdentityAfterCheckin(user, eventId);
            result.put("datingIdentity", identity);
        } catch (Exception ignored) {
        }
    }

    private void appendDatingIdentityForGuest(
            PlatformEvent event,
            EventGuestParticipant guest,
            String eventId,
            Map<String, Object> result
    ) {
        if (!datingEventService.isDatingEvent(event)) {
            return;
        }
        try {
            Map<String, Object> identity = datingEventService.ensureIdentityAfterGuestCheckin(guest, eventId);
            result.put("datingIdentity", identity);
        } catch (Exception ignored) {
        }
    }

    public List<Map<String, Object>> listReviewCandidates(User user, String eventId) {
        PlatformEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        requireEventEnded(event);
        EventSignup mySignup = signupRepository.findByEventIdAndUserId(eventId, user.getUserid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先报名并签到"));
        if (!Boolean.TRUE.equals(mySignup.getCheckedIn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先完成现场签到");
        }
        List<EventSignup> checkedIns = signupRepository.findByEventIdAndCheckedInTrue(eventId);
        List<Long> userIds = checkedIns.stream()
                .map(EventSignup::getUserId)
                .filter(Objects::nonNull)
                .filter(id -> !id.equals(user.getUserid()))
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> users = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u, (a, b) -> a));
        List<EventPeerReview> myReviews = reviewRepository.findByEventIdAndReviewerUserId(eventId, user.getUserid());
        Set<Long> reviewedIds = myReviews.stream().map(EventPeerReview::getTargetUserId).collect(Collectors.toSet());

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Long uid : userIds) {
            User target = users.get(uid);
            if (target == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("userId", uid);
            row.put("nickname", target.getUsername());
            row.put("avatarUrl", target.getProfilePhoto());
            row.put("reviewed", reviewedIds.contains(uid));
            rows.add(row);
        }
        return rows;
    }

    @Transactional
    public Map<String, Object> submitReview(User user, String eventId, Long targetUserId, Integer rating, String comment) {
        PlatformEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        requireEventEnded(event);
        if (targetUserId == null || targetUserId.equals(user.getUserid())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "评分需在 1-5 之间");
        }
        EventSignup mySignup = signupRepository.findByEventIdAndUserId(eventId, user.getUserid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先报名活动"));
        if (!Boolean.TRUE.equals(mySignup.getCheckedIn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先完成现场签到");
        }
        EventSignup targetSignup = signupRepository.findByEventIdAndUserId(eventId, targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "对方未参与本次活动"));
        if (!Boolean.TRUE.equals(targetSignup.getCheckedIn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "对方尚未签到，暂不可评价");
        }
        String safeComment = comment == null ? null : comment.trim();
        if (safeComment != null && safeComment.length() > 300) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "评价内容过长");
        }
        EventPeerReview review = reviewRepository
                .findByEventIdAndReviewerUserIdAndTargetUserId(eventId, user.getUserid(), targetUserId)
                .orElseGet(EventPeerReview::new);
        review.setEventId(eventId);
        review.setReviewerUserId(user.getUserid());
        review.setTargetUserId(targetUserId);
        review.setRating(rating);
        review.setComment(safeComment);
        reviewRepository.save(review);
        return Map.of("saved", true, "rating", rating);
    }

    public static String generateCheckinCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /** 某用户在某活动中尚未互评的人数（已签到前提下） */
    public int countPendingReviews(Long userId, String eventId) {
        EventSignup mySignup = signupRepository.findByEventIdAndUserId(eventId, userId).orElse(null);
        if (mySignup == null || !Boolean.TRUE.equals(mySignup.getCheckedIn())) {
            return 0;
        }
        List<EventSignup> checkedIns = signupRepository.findByEventIdAndCheckedInTrue(eventId);
        long totalTargets = checkedIns.stream()
                .map(EventSignup::getUserId)
                .filter(id -> !id.equals(userId))
                .distinct()
                .count();
        if (totalTargets <= 0) {
            return 0;
        }
        List<EventPeerReview> myReviews = reviewRepository.findByEventIdAndReviewerUserId(eventId, userId);
        Set<Long> reviewedIds = myReviews.stream().map(EventPeerReview::getTargetUserId).collect(Collectors.toSet());
        return (int) checkedIns.stream()
                .map(EventSignup::getUserId)
                .filter(id -> !id.equals(userId))
                .filter(id -> !reviewedIds.contains(id))
                .distinct()
                .count();
    }

    public Map<String, Object> buildReviewSummary(Long userId, String eventId, boolean checkedIn, boolean eventEnded) {
        Map<String, Object> summary = new LinkedHashMap<>();
        if (!checkedIn || !eventEnded) {
            summary.put("pendingReviewCount", 0);
            summary.put("reviewCompleted", !checkedIn);
            summary.put("canReview", false);
            return summary;
        }
        int pending = countPendingReviews(userId, eventId);
        summary.put("pendingReviewCount", pending);
        summary.put("reviewCompleted", pending <= 0);
        summary.put("canReview", true);
        return summary;
    }

    private void requireEventEnded(PlatformEvent event) {
        if (!PlatformEventLifecycle.isEnded(event, LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动尚未结束，暂不可互评");
        }
    }
}
