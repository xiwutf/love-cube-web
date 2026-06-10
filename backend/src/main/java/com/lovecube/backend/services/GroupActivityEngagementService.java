package com.lovecube.backend.services;

import com.lovecube.backend.entity.GroupActivityPeerReview;
import com.lovecube.backend.entity.PlatGroupActivity;
import com.lovecube.backend.entity.PlatGroupActivitySignup;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.GroupActivityPeerReviewRepository;
import com.lovecube.backend.repository.PlatGroupActivityRepository;
import com.lovecube.backend.repository.PlatGroupActivitySignupRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupActivityEngagementService {

    private final PlatGroupActivityRepository activityRepository;
    private final PlatGroupActivitySignupRepository signupRepository;
    private final GroupActivityPeerReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public GroupActivityEngagementService(
            PlatGroupActivityRepository activityRepository,
            PlatGroupActivitySignupRepository signupRepository,
            GroupActivityPeerReviewRepository reviewRepository,
            UserRepository userRepository
    ) {
        this.activityRepository = activityRepository;
        this.signupRepository = signupRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Map<String, Object> generateCheckinCode(Long groupId, Long activityId) {
        PlatGroupActivity activity = requireActivity(groupId, activityId);
        if (!"published".equals(activity.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动不可开启签到");
        }
        String code = EventEngagementService.generateCheckinCode();
        activity.setCheckinCode(code);
        activity.setUpdatedAt(LocalDateTime.now());
        activityRepository.save(activity);
        return Map.of("checkinCode", code, "message", "签到码已生成");
    }

    @Transactional
    public Map<String, Object> checkin(User user, Long groupId, Long activityId, String code) {
        PlatGroupActivity activity = requireActivity(groupId, activityId);
        String expected = activity.getCheckinCode();
        if (expected == null || expected.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动尚未开启签到");
        }
        if (code == null || !expected.equalsIgnoreCase(code.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "签到码不正确");
        }
        PlatGroupActivitySignup signup = signupRepository.findByActivityIdAndUserId(activityId, user.getUserid())
                .filter(s -> "signed_up".equals(s.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先报名活动"));
        if (Boolean.TRUE.equals(signup.getCheckedIn())) {
            return Map.of(
                    "checkedIn", true,
                    "alreadyCheckedIn", true,
                    "checkedInAt", signup.getCheckedInAt() != null ? signup.getCheckedInAt().toString() : null,
                    "message", "你已签到，无需重复操作"
            );
        }
        signup.setCheckedIn(true);
        signup.setCheckedInAt(LocalDateTime.now());
        signup.setUpdatedAt(LocalDateTime.now());
        signupRepository.save(signup);
        return Map.of(
                "checkedIn", true,
                "alreadyCheckedIn", false,
                "checkedInAt", signup.getCheckedInAt().toString(),
                "message", "签到成功"
        );
    }

    public List<Map<String, Object>> listReviewCandidates(User user, Long groupId, Long activityId) {
        requireCheckedInSignup(user, groupId, activityId);
        List<PlatGroupActivitySignup> checkedIns = signupRepository
                .findByActivityIdAndStatusAndCheckedInTrue(activityId, "signed_up");
        List<Long> userIds = checkedIns.stream()
                .map(PlatGroupActivitySignup::getUserId)
                .filter(id -> !id.equals(user.getUserid()))
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> users = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u, (a, b) -> a));
        Set<Long> reviewedIds = reviewRepository.findByActivityIdAndReviewerUserId(activityId, user.getUserid())
                .stream()
                .map(GroupActivityPeerReview::getTargetUserId)
                .collect(Collectors.toSet());

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
    public Map<String, Object> submitReview(User user, Long groupId, Long activityId,
                                            Long targetUserId, Integer rating, String comment) {
        if (targetUserId == null || targetUserId.equals(user.getUserid())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数不合法");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "评分需在 1-5 之间");
        }
        requireCheckedInSignup(user, groupId, activityId);
        PlatGroupActivitySignup targetSignup = signupRepository.findByActivityIdAndUserId(activityId, targetUserId)
                .filter(s -> "signed_up".equals(s.getStatus()) && Boolean.TRUE.equals(s.getCheckedIn()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "对方尚未签到，暂不可评价"));
        String safeComment = comment == null ? null : comment.trim();
        if (safeComment != null && safeComment.length() > 300) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "评价内容过长");
        }
        GroupActivityPeerReview review = reviewRepository
                .findByActivityIdAndReviewerUserIdAndTargetUserId(activityId, user.getUserid(), targetUserId)
                .orElseGet(GroupActivityPeerReview::new);
        review.setActivityId(activityId);
        review.setGroupId(groupId);
        review.setReviewerUserId(user.getUserid());
        review.setTargetUserId(targetUserId);
        review.setRating(rating);
        review.setComment(safeComment);
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }
        reviewRepository.save(review);
        return Map.of("saved", true, "rating", rating);
    }

    public int countPendingReviews(Long userId, Long activityId) {
        PlatGroupActivitySignup mySignup = signupRepository.findByActivityIdAndUserId(activityId, userId).orElse(null);
        if (mySignup == null || !Boolean.TRUE.equals(mySignup.getCheckedIn())) {
            return 0;
        }
        List<PlatGroupActivitySignup> checkedIns = signupRepository
                .findByActivityIdAndStatusAndCheckedInTrue(activityId, "signed_up");
        Set<Long> reviewedIds = reviewRepository.findByActivityIdAndReviewerUserId(activityId, userId)
                .stream()
                .map(GroupActivityPeerReview::getTargetUserId)
                .collect(Collectors.toSet());
        return (int) checkedIns.stream()
                .map(PlatGroupActivitySignup::getUserId)
                .filter(id -> !id.equals(userId))
                .filter(id -> !reviewedIds.contains(id))
                .distinct()
                .count();
    }

    public Map<String, Object> buildReviewSummary(Long userId, Long activityId, boolean checkedIn, boolean activityEnded) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("canReview", checkedIn && activityEnded);
        if (!checkedIn || !activityEnded) {
            summary.put("pendingReviewCount", 0);
            summary.put("reviewCompleted", true);
            return summary;
        }
        int pending = countPendingReviews(userId, activityId);
        summary.put("pendingReviewCount", pending);
        summary.put("reviewCompleted", pending <= 0);
        return summary;
    }

    private PlatGroupActivitySignup requireCheckedInSignup(User user, Long groupId, Long activityId) {
        requireActivity(groupId, activityId);
        PlatGroupActivitySignup signup = signupRepository.findByActivityIdAndUserId(activityId, user.getUserid())
                .filter(s -> "signed_up".equals(s.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先报名活动"));
        if (!Boolean.TRUE.equals(signup.getCheckedIn())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请先完成现场签到");
        }
        return signup;
    }

    private PlatGroupActivity requireActivity(Long groupId, Long activityId) {
        return activityRepository.findByIdAndGroupId(activityId, groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
    }
}
