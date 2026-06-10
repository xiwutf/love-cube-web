package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.Announcement;
import com.lovecube.backend.entity.Article;
import com.lovecube.backend.entity.FellowshipProfile;
import com.lovecube.backend.entity.FellowshipProfileMain;
import com.lovecube.backend.entity.PlatGroupPost;
import com.lovecube.backend.entity.PlatformEvent;
import com.lovecube.backend.entity.ReportRecord;
import com.lovecube.backend.entity.UserFeedback;
import com.lovecube.backend.entity.UserPhoto;
import com.lovecube.backend.entity.UserProfile;
import com.lovecube.backend.entity.UserVerification;
import com.lovecube.backend.entity.VerificationRequest;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.AnnouncementRepository;
import com.lovecube.backend.repository.ArticleRepository;
import com.lovecube.backend.repository.FellowshipProfileMainRepository;
import com.lovecube.backend.repository.FellowshipProfileRepository;
import com.lovecube.backend.repository.PlatGroupPostRepository;
import com.lovecube.backend.repository.PlatformEventRepository;
import com.lovecube.backend.repository.ReportRecordRepository;
import com.lovecube.backend.repository.UserFeedbackRepository;
import com.lovecube.backend.repository.UserPhotoRepository;
import com.lovecube.backend.repository.UserProfileRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.repository.UserVerificationRepository;
import com.lovecube.backend.repository.VerificationRequestRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.AdminDashboardStatsService;
import com.lovecube.backend.services.FellowshipInviteService;
import com.lovecube.backend.services.HomeConfigService;
import com.lovecube.backend.services.NotificationService;
import com.lovecube.backend.services.PermissionConstants;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminContentController {
    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "ADMIN", "SUPER_ADMIN", "ROOT");
    private static final Set<String> ADMIN_MANAGEABLE_ROLES = Set.of("USER", "ADMIN");
    private static final Set<String> HIGH_PRIVILEGE_ROLES = Set.of("SUPER_ADMIN", "ROOT");
    private static final String HIDDEN_SUPER_ADMIN_PHONE = "15030251407";

    private final AnnouncementRepository announcementRepository;
    private final ArticleRepository articleRepository;
    private final PlatformEventRepository platformEventRepository;
    private final UserRepository userRepository;
    private final VerificationRequestRepository verificationRequestRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final ReportRecordRepository reportRecordRepository;
    private final PlatGroupPostRepository platGroupPostRepository;
    private final UserFeedbackRepository userFeedbackRepository;
    private final UserPhotoRepository userPhotoRepository;
    private final FellowshipProfileMainRepository fellowshipProfileMainRepository;
    private final FellowshipProfileRepository fellowshipProfileRepository;
    private final UserProfileRepository userProfileRepository;
    private final AdminDashboardStatsService adminDashboardStatsService;
    private final AdminAuthService adminAuthService;
    private final FellowshipInviteService fellowshipInviteService;
    private final NotificationService notificationService;
    private final HomeConfigService homeConfigService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminContentController(
            AnnouncementRepository announcementRepository,
            ArticleRepository articleRepository,
            PlatformEventRepository platformEventRepository,
            UserRepository userRepository,
            VerificationRequestRepository verificationRequestRepository,
            UserVerificationRepository userVerificationRepository,
            ReportRecordRepository reportRecordRepository,
            PlatGroupPostRepository platGroupPostRepository,
            UserFeedbackRepository userFeedbackRepository,
            UserPhotoRepository userPhotoRepository,
            FellowshipProfileMainRepository fellowshipProfileMainRepository,
            FellowshipProfileRepository fellowshipProfileRepository,
            UserProfileRepository userProfileRepository,
            AdminDashboardStatsService adminDashboardStatsService,
            AdminAuthService adminAuthService,
            FellowshipInviteService fellowshipInviteService,
            NotificationService notificationService,
            HomeConfigService homeConfigService,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.announcementRepository = announcementRepository;
        this.articleRepository = articleRepository;
        this.platformEventRepository = platformEventRepository;
        this.userRepository = userRepository;
        this.verificationRequestRepository = verificationRequestRepository;
        this.userVerificationRepository = userVerificationRepository;
        this.reportRecordRepository = reportRecordRepository;
        this.platGroupPostRepository = platGroupPostRepository;
        this.userFeedbackRepository = userFeedbackRepository;
        this.userPhotoRepository = userPhotoRepository;
        this.fellowshipProfileMainRepository = fellowshipProfileMainRepository;
        this.fellowshipProfileRepository = fellowshipProfileRepository;
        this.userProfileRepository = userProfileRepository;
        this.adminDashboardStatsService = adminDashboardStatsService;
        this.adminAuthService = adminAuthService;
        this.fellowshipInviteService = fellowshipInviteService;
        this.notificationService = notificationService;
        this.homeConfigService = homeConfigService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/home-config")
    public Map<String, Object> getHomeConfig(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.SYSTEM_MANAGE);
        return homeConfigService.getAdminHomeConfig();
    }

    @GetMapping("/auth-context")
    public Map<String, Object> getAuthContext(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = adminAuthService.requireUser(authHeader);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getUserid());
        result.put("nickname", user.getUsername() != null ? user.getUsername() : "");
        result.put("roles", adminAuthService.getUserRoles(user));
        result.put("permissions", adminAuthService.getUserPermissions(user));
        result.put("isAdmin", adminAuthService.isAdmin(user));
        return result;
    }

    @PutMapping("/home-config")
    public Map<String, Object> saveHomeConfig(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.SYSTEM_MANAGE);
        return homeConfigService.saveAdminHomeConfig(payload);
    }

    @GetMapping("/announcements")
    public List<Announcement> listAnnouncements(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_ANNOUNCEMENT_MANAGE);
        return announcementRepository.findAll();
    }

    @PostMapping("/announcements")
    public Announcement saveAnnouncement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Announcement payload
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_ANNOUNCEMENT_MANAGE);
        Optional<Announcement> previous = Optional.empty();
        if (payload.getId() != null && !payload.getId().isBlank()) {
            previous = announcementRepository.findById(payload.getId());
        }
        if (payload.getId() == null || payload.getId().isBlank()) {
            payload.setId("announcement-" + UUID.randomUUID());
        }
        if (payload.getStatus() == null || payload.getStatus().isBlank()) {
            payload.setStatus("draft");
        }
        if (payload.getPublishDate() == null && "published".equals(payload.getStatus())) {
            payload.setPublishDate(LocalDateTime.now());
        }
        if (payload.getPopupEnabled() == null) {
            payload.setPopupEnabled(false);
        }
        String prevStatus = previous.map(Announcement::getStatus).orElse("");
        Announcement saved = announcementRepository.save(payload);
        boolean nowPublished = "published".equalsIgnoreCase(saved.getStatus());
        boolean wasPublished = prevStatus != null && "published".equalsIgnoreCase(prevStatus);
        if (nowPublished && !wasPublished && (Boolean.TRUE.equals(saved.getPinned()) || Boolean.TRUE.equals(saved.getPopupEnabled()))) {
            try {
                notificationService.broadcastPlatformAnnouncement(
                        saved.getTitle(),
                        saved.getSummary() != null && !saved.getSummary().isBlank() ? saved.getSummary() : saved.getTitle(),
                        saved.getId(),
                        "/platform/announcements?id=" + saved.getId());
            } catch (Exception ignored) {
            }
        }
        return saved;
    }

    @GetMapping("/articles")
    public List<Article> listArticles(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_ARTICLE_MANAGE);
        return articleRepository.findAll();
    }

    @PostMapping("/articles")
    public Article saveArticle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Article payload
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_ARTICLE_MANAGE);
        if (payload.getId() == null || payload.getId().isBlank()) {
            payload.setId("article-" + UUID.randomUUID());
        }
        if (payload.getStatus() == null || payload.getStatus().isBlank()) {
            payload.setStatus("draft");
        }
        if (payload.getPublishDate() == null && "published".equals(payload.getStatus())) {
            payload.setPublishDate(LocalDateTime.now());
        }
        return articleRepository.save(payload);
    }

    @GetMapping("/events")
    public List<PlatformEvent> listEvents(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_EVENT_MANAGE);
        return platformEventRepository.findAll();
    }

    @PostMapping("/events")
    public PlatformEvent saveEvent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody PlatformEvent payload
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_EVENT_MANAGE);
        if (payload.getId() == null || payload.getId().isBlank()) {
            payload.setId("event-" + UUID.randomUUID());
        }
        if (payload.getStatus() == null || payload.getStatus().isBlank()) {
            payload.setStatus("draft");
        }
        if (payload.getSignupCount() == null) {
            payload.setSignupCount(0);
        }
        if ("published".equals(payload.getStatus())
                && (payload.getCheckinCode() == null || payload.getCheckinCode().isBlank())) {
            payload.setCheckinCode(com.lovecube.backend.services.EventEngagementService.generateCheckinCode());
        }
        return platformEventRepository.save(payload);
    }

    @GetMapping("/users")
    public List<Map<String, Object>> listUsers(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User operator = adminAuthService.requirePermission(authHeader, PermissionConstants.USER_MANAGE);
        boolean hiddenSuperAdminOperator = adminAuthService.isHiddenSuperAdmin(operator);
        List<User> visibleUsers = userRepository.findAll().stream()
                .filter(user -> hiddenSuperAdminOperator || !adminAuthService.isHiddenSuperAdmin(user))
                .collect(Collectors.toList());
        List<Long> userIds = visibleUsers.stream()
                .map(User::getUserid)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        Map<Long, Long> uploadedPhotoCountMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<Object[]> groupedCounts = userPhotoRepository.countGroupedByUserIds(userIds);
            for (Object[] row : groupedCounts) {
                if (row == null || row.length < 2) {
                    continue;
                }
                Long uid = row[0] instanceof Number ? ((Number) row[0]).longValue() : null;
                Long count = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
                if (uid != null) {
                    uploadedPhotoCountMap.put(uid, count);
                }
            }
        }
        Map<Long, FellowshipProfileMain> mainProfileMap = userIds.isEmpty()
                ? Map.of()
                : fellowshipProfileMainRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(FellowshipProfileMain::getUserId, item -> item, (a, b) -> a));
        Map<Long, FellowshipProfile> legacyProfileMap = userIds.isEmpty()
                ? Map.of()
                : fellowshipProfileRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(FellowshipProfile::getUserId, item -> item, (a, b) -> a));
        Map<Long, UserProfile> userProfileMap = userIds.isEmpty()
                ? Map.of()
                : userProfileRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, item -> item, (a, b) -> a));
        Map<Long, UserVerification> latestUvByUser = userIds.isEmpty()
                ? Map.of()
                : buildLatestUserVerificationByUserId(userVerificationRepository.findByUserIdIn(userIds));
        Map<Long, VerificationRequest> latestVrByUser = userIds.isEmpty()
                ? Map.of()
                : buildLatestVerificationRequestByUserId(verificationRequestRepository.findByUserIdIn(userIds));

        return visibleUsers.stream()
                .map(user -> {
                    FellowshipProfileMain mainProfile = mainProfileMap.get(user.getUserid());
                    FellowshipProfile legacyProfile = legacyProfileMap.get(user.getUserid());
                    UserProfile userProfile = userProfileMap.get(user.getUserid());
                    long uploadedPhotoCount = uploadedPhotoCountMap.getOrDefault(user.getUserid(), 0L);
                    Integer genderCode = user.getGender();
                    if (genderCode == null) {
                        genderCode = parseGenderCodeFromText(mainProfile == null ? null : mainProfile.getGender());
                    }
                    Map<String, Object> item = new HashMap<>();
                    item.put("userId", user.getUserid());
                    item.put("username", user.getUsername() == null ? "" : user.getUsername());
                    item.put("phone", user.getPhoneNumber() == null ? "" : user.getPhoneNumber());
                    item.put("avatarUrl", resolveAdminUserAvatar(user, legacyProfile, mainProfile, userProfile));
                    item.put("gender", genderCode);
                    item.put("age", resolveAdminListAge(user, mainProfile));
                    item.put("role", normalizeRoleForView(user.getRole(), adminAuthService.isAdmin(user)));
                    item.put("status", "DISABLED".equalsIgnoreCase(user.getUserStatus()) ? "disabled" : "active");
                    item.put("fellowshipEnabled", Boolean.TRUE.equals(user.getFellowshipEnabled()));
                    item.put("uploadedPhotoCount", uploadedPhotoCount);
                    item.put("hasUploadedPhotos", uploadedPhotoCount > 0);
                    item.put("verificationStatus", resolveAdminListVerificationStatus(
                            user.getUserid(), latestUvByUser, latestVrByUser));
                    item.put("inviteCode", user.getInviteCode() == null ? "" : user.getInviteCode());
                    item.put("invitedByUserId", user.getInvitedByUserId());
                    item.put("createdAt", user.getCreatedAt());
                    item.put("canForceDelete", hiddenSuperAdminOperator
                            && !operator.getUserid().equals(user.getUserid())
                            && !adminAuthService.isHiddenSuperAdmin(user));
                    item.put("canResetPassword", hiddenSuperAdminOperator
                            && !operator.getUserid().equals(user.getUserid())
                            && !adminAuthService.isHiddenSuperAdmin(user));
                    return item;
                }).collect(Collectors.toList());
    }

    @GetMapping("/users/{userId}/photos")
    public Map<String, Object> listUserPhotos(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId
    ) {
        User operator = adminAuthService.requirePermission(authHeader, PermissionConstants.USER_MANAGE);
        User target = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        if (!adminAuthService.isHiddenSuperAdmin(operator) && adminAuthService.isHiddenSuperAdmin(target)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }

        List<Map<String, Object>> photos = userPhotoRepository.findByUserIdOrderBySortOrderAscIdAsc(userId).stream()
                .map(this::toAdminUserPhotoView)
                .collect(Collectors.toList());

        return Map.of(
                "userId", userId,
                "photos", photos,
                "photosCount", photos.size()
        );
    }

    @PutMapping("/users/{userId}/role")
    public Map<String, Object> updateUserRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId,
            @RequestBody Map<String, Object> payload
    ) {
        User operator = adminAuthService.requirePermission(authHeader, PermissionConstants.USER_MANAGE);

        String rawRole = String.valueOf(payload.getOrDefault("role", "")).trim().toUpperCase();
        if (!ALLOWED_ROLES.contains(rawRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role 仅支持 USER/ADMIN/SUPER_ADMIN/ROOT");
        }
        String operatorRole = resolveOperatorRole(operator);

        User target = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        if (!adminAuthService.isHiddenSuperAdmin(operator) && adminAuthService.isHiddenSuperAdmin(target)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }
        String targetRole = resolveOperatorRole(target);

        if (!"ROOT".equals(operatorRole)) {
            if (HIGH_PRIVILEGE_ROLES.contains(targetRole)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅 ROOT 可修改 ROOT/SUPER_ADMIN 角色");
            }
            if (!ADMIN_MANAGEABLE_ROLES.contains(rawRole)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "普通管理员仅可设置 USER/ADMIN");
            }
        }

        if (operator.getUserid().equals(target.getUserid()) && "USER".equals(rawRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能将当前登录管理员降级为 USER");
        }

        target.setRole(rawRole);
        userRepository.save(target);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", target.getUserid());
        result.put("role", normalizeRoleForView(target.getRole(), adminAuthService.isAdmin(target)));
        result.put("message", "角色已更新");
        return result;
    }

    @GetMapping("/invites")
    public List<Map<String, Object>> listInvites(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "inviterUserId", required = false) Long inviterUserId,
            @RequestParam(value = "inviteeUserId", required = false) Long inviteeUserId,
            @RequestParam(value = "inviteCode", required = false) String inviteCode,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "status", required = false) String status
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.USER_MANAGE);
        return fellowshipInviteService.searchInvites(
                inviterUserId,
                inviteeUserId,
                inviteCode,
                parseDateTime(startTime, false),
                parseDateTime(endTime, true),
                status
        );
    }

    @GetMapping("/verifications")
    public List<UserVerification> listVerifications(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.REVIEW_MANAGE);
        return userVerificationRepository.findAllByOrderBySubmittedAtDesc();
    }

    @PostMapping("/verifications")
    public VerificationRequest saveVerification(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody VerificationRequest payload
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.REVIEW_MANAGE);
        if (payload.getId() == null || payload.getId().isBlank()) {
            payload.setId("verify-" + UUID.randomUUID());
        }
        if (payload.getSubmittedAt() == null) {
            payload.setSubmittedAt(LocalDateTime.now());
        }
        if (payload.getStatus() == null || payload.getStatus().isBlank()) {
            payload.setStatus("pending");
        }
        return verificationRequestRepository.save(payload);
    }

    @GetMapping("/reports")
    public List<ReportRecord> listReports(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.REVIEW_MANAGE);
        List<ReportRecord> records = reportRecordRepository.findAll();
        enrichGroupPostReports(records);
        return records;
    }

    private void enrichGroupPostReports(List<ReportRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> postIds = records.stream()
                .filter(r -> "GROUP_POST".equalsIgnoreCase(
                        r.getTargetType() != null ? r.getTargetType() : ""))
                .map(r -> parseReportPostId(r.getTargetId()))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (postIds.isEmpty()) {
            return;
        }
        Map<Long, Long> postToGroup = new HashMap<>();
        for (PlatGroupPost post : platGroupPostRepository.findAllById(postIds)) {
            if (post.getGroupId() != null) {
                postToGroup.put(post.getId(), post.getGroupId());
            }
        }
        for (ReportRecord record : records) {
            if (!"GROUP_POST".equalsIgnoreCase(
                    record.getTargetType() != null ? record.getTargetType() : "")) {
                continue;
            }
            Long postId = parseReportPostId(record.getTargetId());
            if (postId != null) {
                record.setGroupId(postToGroup.get(postId));
            }
        }
    }

    private Long parseReportPostId(String targetId) {
        if (targetId == null || targetId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(targetId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @GetMapping("/feedbacks")
    public List<UserFeedback> listFeedbacks(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_MANAGE);
        return userFeedbackRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/feedbacks/summary")
    public Map<String, Object> feedbackSummary(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_MANAGE);
        List<UserFeedback> feedbacks = userFeedbackRepository.findAllByOrderByCreatedAtDesc();
        Map<String, Long> moduleCount = new HashMap<>();
        Map<String, Long> goalCount = new HashMap<>();
        Map<String, Long> improvementCount = new HashMap<>();
        long total = feedbacks.size();
        for (UserFeedback feedback : feedbacks) {
            String module = normalizeStatOption(extractField(feedback.getContent(), "Q1-最关注模块："));
            if (module.isBlank()) {
                module = "未填写";
            }
            moduleCount.put(module, moduleCount.getOrDefault(module, 0L) + 1);

            String goals = extractField(feedback.getContent(), "Q2-来站目标：");
            if (goals.isBlank()) {
                goalCount.put("未填写", goalCount.getOrDefault("未填写", 0L) + 1);
            } else {
                Set<String> uniqueGoals = splitMultiOptions(goals).stream()
                        .map(this::normalizeStatOption)
                        .filter(item -> !item.isBlank())
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                if (uniqueGoals.isEmpty()) {
                    uniqueGoals.add("未填写");
                }
                for (String goal : uniqueGoals) {
                    goalCount.put(goal, goalCount.getOrDefault(goal, 0L) + 1);
                }
            }

            String improvement = normalizeStatOption(extractField(feedback.getContent(), "Q3-最需要改进："));
            if (improvement.isBlank()) {
                improvement = normalizeStatOption(extractField(feedback.getContent(), "Q3-当前最缺："));
            }
            if (improvement.isBlank()) {
                improvement = "未填写";
            }
            improvementCount.put(improvement, improvementCount.getOrDefault(improvement, 0L) + 1);
        }

        List<Map<String, Object>> moduleRanking = buildRanking(moduleCount, total, "module");
        List<Map<String, Object>> goalRanking = buildRanking(goalCount, total, "goal");
        List<Map<String, Object>> improvementRanking = buildRanking(improvementCount, total, "improvement");

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("moduleRanking", moduleRanking);
        result.put("goalRanking", goalRanking);
        result.put("improvementRanking", improvementRanking);
        return result;
    }

    @PostMapping("/reports")
    public ReportRecord saveReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ReportRecord payload
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.REVIEW_MANAGE);
        if (payload.getId() == null || payload.getId().isBlank()) {
            payload.setId("report-" + UUID.randomUUID());
        }
        if (payload.getStatus() == null || payload.getStatus().isBlank()) {
            payload.setStatus("pending");
        }
        if (payload.getCreatedAt() == null) {
            payload.setCreatedAt(LocalDateTime.now());
        }
        return reportRecordRepository.save(payload);
    }

    @PatchMapping("/reports/{id}")
    public ReportRecord updateReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestBody Map<String, Object> payload
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.REVIEW_MANAGE);
        ReportRecord record = reportRecordRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "举报记录不存在"));
        if (payload.containsKey("status")) {
            record.setStatus(String.valueOf(payload.get("status")));
        }
        if (payload.containsKey("note")) {
            record.setNote(String.valueOf(payload.get("note")));
        }
        return reportRecordRepository.save(record);
    }

    @PatchMapping("/reports/{id}/review")
    public Map<String, Object> reviewReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestBody Map<String, Object> payload
    ) {
        User admin = adminAuthService.requirePermission(authHeader, PermissionConstants.REVIEW_MANAGE);
        ReportRecord record = reportRecordRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "举报记录不存在"));

        String action = String.valueOf(payload.getOrDefault("action", "")).trim().toLowerCase();
        String note   = payload.containsKey("note") ? String.valueOf(payload.get("note")) : null;

        Long hiddenGroupPostGroupId = null;
        switch (action) {
            case "reviewed" -> record.setStatus("REVIEWED");
            case "rejected" -> record.setStatus("REJECTED");
            case "hide_post" -> {
                hiddenGroupPostGroupId = hideReportedGroupPost(record);
                record.setStatus("REVIEWED");
            }
            case "banned" -> {
                record.setStatus("BANNED");
                if (record.getTargetUserId() != null) {
                    userRepository.findById(record.getTargetUserId()).ifPresent(target -> {
                        target.setUserStatus("DISABLED");
                        userRepository.save(target);
                    });
                }
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "action 必须为 reviewed / rejected / banned / hide_post");
        }

        record.setReviewedAt(LocalDateTime.now());
        record.setReviewedBy(admin.getUserid());
        if (note != null && !note.isBlank()) record.setNote(note);
        reportRecordRepository.save(record);

        // 通知举报人处理结果
        if (record.getReporterId() != null) {
            boolean groupPost = "GROUP_POST".equalsIgnoreCase(
                    record.getTargetType() != null ? record.getTargetType() : "");
            String notifContent = switch (action) {
                case "hide_post" -> groupPost
                        ? "你举报的团体动态已下架，感谢你的反馈。"
                        : "你举报的内容已处理，感谢你的反馈。";
                case "reviewed" -> groupPost
                        ? "你举报的团体动态经核实已处理，感谢你的反馈。"
                        : "你举报的用户经核实已被处理，感谢你的反馈。";
                case "rejected" -> "你的举报经核实不符合规范，已予以驳回。";
                case "banned"   -> "你举报的用户经核实已被封禁，感谢你维护社区环境。";
                default         -> "你的举报已处理。";
            };
            try {
                notificationService.send(record.getReporterId(), "REPORT_HANDLED",
                    "举报处理结果", notifContent, "REPORT", record.getId());
            } catch (Exception ignored) {}
        }

        // 封禁时通知被封禁用户
        if ("banned".equals(action) && record.getTargetUserId() != null) {
            try {
                notificationService.send(record.getTargetUserId(), "BANNED",
                    "账号已被封禁", "你的账号因违反平台规范已被封禁，如有异议请联系客服。",
                    null, null);
            } catch (Exception ignored) {}
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", record.getId());
        result.put("status", record.getStatus());
        result.put("reviewedAt", record.getReviewedAt());
        result.put("reviewedBy", record.getReviewedBy());
        result.put("message", switch (action) {
            case "hide_post" -> "团体动态已下架";
            case "reviewed" -> "举报已标记为已审核";
            case "rejected" -> "举报已驳回";
            case "banned"   -> "用户已封禁，举报已处理";
            default -> "处理完成";
        });
        if (hiddenGroupPostGroupId != null) {
            result.put("groupId", hiddenGroupPostGroupId);
            result.put("postId", record.getTargetId());
        }
        return result;
    }

    private Long hideReportedGroupPost(ReportRecord record) {
        if (!"GROUP_POST".equalsIgnoreCase(record.getTargetType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "仅团体动态举报可下架");
        }
        if (record.getTargetId() == null || record.getTargetId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少动态 ID");
        }
        Long postId;
        try {
            postId = Long.parseLong(record.getTargetId().trim());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "动态 ID 格式不正确");
        }
        PlatGroupPost post = platGroupPostRepository.findById(postId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "团体动态不存在"));
        post.setStatus("deleted");
        platGroupPostRepository.save(post);
        if (post.getUserId() != null) {
            try {
                notificationService.send(post.getUserId(), "CONTENT_MODERATION_REJECTED",
                        "团体动态已下架",
                        "你发布的团体动态因违反社区规范已被下架。",
                        "platform_group",
                        String.valueOf(post.getGroupId()));
            } catch (Exception ignored) {
            }
        }
        return post.getGroupId();
    }

    @PatchMapping("/feedbacks/{id}")
    public UserFeedback updateFeedback(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestBody Map<String, Object> payload
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_MANAGE);
        UserFeedback record = userFeedbackRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "反馈记录不存在"));

        if (payload.containsKey("status")) {
            String status = String.valueOf(payload.get("status")).trim().toLowerCase();
            if (!Set.of("pending", "processing", "resolved").contains(status)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status 必须为 pending/processing/resolved");
            }
            record.setStatus(status);
        }
        if (payload.containsKey("adminNote")) {
            record.setAdminNote(String.valueOf(payload.get("adminNote")));
        }
        return userFeedbackRepository.save(record);
    }

    @DeleteMapping("/announcements/{id}")
    public Map<String, Object> deleteAnnouncement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_ANNOUNCEMENT_MANAGE);
        announcementRepository.deleteById(id);
        return Map.of("message", "公告已删除", "id", id);
    }

    @DeleteMapping("/articles/{id}")
    public Map<String, Object> deleteArticle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_ARTICLE_MANAGE);
        articleRepository.deleteById(id);
        return Map.of("message", "资讯已删除", "id", id);
    }

    @DeleteMapping("/events/{id}")
    public Map<String, Object> deleteEvent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_EVENT_MANAGE);
        platformEventRepository.deleteById(id);
        return Map.of("message", "活动已删除", "id", id);
    }

    @PatchMapping("/verifications/{id}/review")
    public UserVerification reviewVerification(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        User admin = adminAuthService.requirePermission(authHeader, PermissionConstants.REVIEW_MANAGE);
        UserVerification record = userVerificationRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "认证记录不存在"));

        String action = String.valueOf(payload.getOrDefault("action", "")).trim().toLowerCase();
        String reason = payload.containsKey("reason") ? String.valueOf(payload.get("reason")) : null;

        if ("approve".equals(action)) {
            record.setStatus("approved");
            record.setReviewedAt(LocalDateTime.now());
            record.setReviewerId(admin.getUserid());
        } else if ("reject".equals(action)) {
            record.setStatus("rejected");
            record.setReviewedAt(LocalDateTime.now());
            record.setReviewerId(admin.getUserid());
            if (reason != null && !reason.isBlank()) record.setRejectReason(reason);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "action 必须为 approve/reject");
        }
        userVerificationRepository.save(record);

        // Notify user of result
        if (record.getUserId() != null) {
            String typeLabel = switch (record.getVerifyType() == null ? "" : record.getVerifyType()) {
                case "PHOTO"    -> "真人头像";
                case "IDCARD"   -> "身份证";
                default          -> "实名";
            };
            try {
                if ("approve".equals(action)) {
                    notificationService.send(record.getUserId(), "SYSTEM",
                        "认证审核通过",
                        typeLabel + "认证已审核通过，你的认证标识已生效。",
                        "USER", String.valueOf(record.getUserId()));
                } else {
                    String rejectMsg = (reason != null && !reason.isBlank())
                        ? reason : "资料不符合要求，请修改后重新提交。";
                    notificationService.send(record.getUserId(), "SYSTEM",
                        "认证审核未通过",
                        typeLabel + "认证未通过：" + rejectMsg,
                        "USER", String.valueOf(record.getUserId()));
                }
            } catch (Exception ignored) {}
        }
        return record;
    }

    @PostMapping("/verifications/{id}/review")
    public UserVerification reviewVerificationByPost(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        return reviewVerification(authHeader, id, payload);
    }

    @PutMapping("/users/{userId}/status")
    public Map<String, Object> updateUserStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId,
            @RequestBody Map<String, Object> payload
    ) {
        User operator = adminAuthService.requirePermission(authHeader, PermissionConstants.USER_MANAGE);

        User target = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        if (!adminAuthService.isHiddenSuperAdmin(operator) && adminAuthService.isHiddenSuperAdmin(target)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }

        String status = String.valueOf(payload.getOrDefault("status", "active")).trim().toLowerCase();
        target.setUserStatus("banned".equals(status) ? "DISABLED" : "NORMAL");
        userRepository.save(target);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("status", "DISABLED".equalsIgnoreCase(target.getUserStatus()) ? "banned" : "active");
        result.put("message", "banned".equals(status) ? "用户已封禁" : "用户已解封");
        return result;
    }

    @PutMapping("/users/{userId}/fellowship")
    public Map<String, Object> updateUserFellowshipStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId,
            @RequestBody Map<String, Object> payload
    ) {
        User operator = adminAuthService.requirePermission(authHeader, PermissionConstants.USER_MANAGE);

        User target = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        if (!adminAuthService.isHiddenSuperAdmin(operator) && adminAuthService.isHiddenSuperAdmin(target)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在");
        }

        boolean fellowshipEnabled = Boolean.parseBoolean(String.valueOf(payload.getOrDefault("fellowshipEnabled", false)));
        target.setFellowshipEnabled(fellowshipEnabled);
        target.setFellowshipMatchVisible(fellowshipEnabled);
        userRepository.save(target);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("fellowshipEnabled", fellowshipEnabled);
        result.put("fellowshipMatchVisible", Boolean.TRUE.equals(target.getFellowshipMatchVisible()));
        result.put("message", fellowshipEnabled ? "联谊模块已开通" : "联谊模块已关闭");
        return result;
    }

    @DeleteMapping("/users/{userId}/force")
    public Map<String, Object> forceDeleteUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId
    ) {
        User operator = adminAuthService.requireUser(authHeader);
        if (!adminAuthService.isHiddenSuperAdmin(operator)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅超级管理员可执行强制删除");
        }
        if (operator.getUserid().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能删除当前登录账号");
        }

        User target = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        if (adminAuthService.isHiddenSuperAdmin(target)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "不能删除超级管理员账号");
        }

        userRepository.delete(target);
        return Map.of(
                "userId", userId,
                "message", "用户已从数据库强制删除"
        );
    }

    @PutMapping("/users/{userId}/reset-password")
    public Map<String, Object> resetUserPassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId
    ) {
        User operator = adminAuthService.requireUser(authHeader);
        if (!adminAuthService.isHiddenSuperAdmin(operator)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅超级管理员可重置密码");
        }
        if (operator.getUserid().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能重置当前登录账号密码");
        }

        User target = userRepository.findById(userId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        if (adminAuthService.isHiddenSuperAdmin(target)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "不能重置超级管理员密码");
        }

        target.setPasswordHash(passwordEncoder.encode("123456"));
        userRepository.save(target);
        return Map.of(
                "userId", userId,
                "message", "密码已重置为 123456"
        );
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(name = "refresh", defaultValue = "false") boolean refresh
    ) {
        User operator = adminAuthService.requireAdmin(authHeader);
        if (adminAuthService.isGroupStewardOnly(operator)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "团体负责人请使用「我的团体」管理，无法查看全站运营数据");
        }
        boolean hiddenSuperAdminOperator = adminAuthService.isHiddenSuperAdmin(operator);
        return adminDashboardStatsService.getDashboardStats(hiddenSuperAdminOperator, refresh);
    }

    private LocalDateTime parseDateTime(String value, boolean toEndOfDay) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ignored) {
        }
        try {
            LocalDate date = LocalDate.parse(value);
            return toEndOfDay ? date.atTime(23, 59, 59) : date.atStartOfDay();
        } catch (Exception ignored) {
        }
        return null;
    }

    private String extractField(String content, String prefix) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            if (line != null && line.startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }
        return "";
    }

    private List<String> splitMultiOptions(String value) {
        if (value == null || value.isBlank()) {
            return List.of("未填写");
        }
        List<String> options = List.of(value.split("[、,，/；;|]")).stream()
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .collect(Collectors.toList());
        return options.isEmpty() ? List.of("未填写") : options;
    }

    private String normalizeStatOption(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value
                .replace('\u3000', ' ')
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .replaceAll("[“”]", "\"")
                .replaceAll("[‘’]", "'")
                .replaceAll("[（]", "(")
                .replaceAll("[）]", ")")
                .replaceAll("[【]", "[")
                .replaceAll("[】]", "]")
                .replaceAll("[，]", ",")
                .replaceAll("[。]", ".")
                .replaceAll("[；]", ";")
                .replaceAll("[：]", ":")
                .trim();
        // Ignore bracket variants to avoid "(联谊", "（联谊）", "联谊)" being split.
        normalized = normalized.replaceAll("[\\(\\)\\[\\]\\{\\}（）【】]", "");
        // Remove leading/trailing symbols that often come from manual input paste.
        normalized = normalized.replaceAll("^[\\p{Punct}\\s]+", "").replaceAll("[\\p{Punct}\\s]+$", "").trim();
        return normalized;
    }

    private List<Map<String, Object>> buildRanking(Map<String, Long> source, long total, String keyName) {
        return source.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put(keyName, entry.getKey());
                    item.put("count", entry.getValue());
                    item.put("percent", total <= 0 ? 0 : Math.round(entry.getValue() * 10000.0 / total) / 100.0);
                    return item;
                })
                .collect(Collectors.toList());
    }

    private String normalizeRoleForView(String role, boolean isAdminFallback) {
        if (role == null || role.isBlank()) {
            return isAdminFallback ? "admin" : "user";
        }
        String normalized = role.trim().toLowerCase();
        if ("super_admin".equals(normalized)) {
            return "super_admin";
        }
        if ("root".equals(normalized)) {
            return "root";
        }
        if ("admin".equals(normalized)) {
            return "admin";
        }
        return "user";
    }

    private String resolveOperatorRole(User user) {
        if (user == null) {
            return "USER";
        }
        if (adminAuthService.isHiddenSuperAdmin(user)) {
            return "ROOT";
        }
        String role = user.getRole();
        if (role != null && !role.isBlank()) {
            String normalized = role.trim().toUpperCase();
            if (ALLOWED_ROLES.contains(normalized)) {
                return normalized;
            }
        }
        return adminAuthService.isAdmin(user) ? "ADMIN" : "USER";
    }

    /**
     * 与 {@link com.lovecube.backend.services.UnifiedProfileService#buildUnifiedProfile} 头像合并顺序一致：
     * fellowship_profile.avatar_url → fellowship_profiles.avatar → user_profiles.avatar → users.profile_photo
     */
    private String resolveAdminUserAvatar(
            User user,
            FellowshipProfile legacyProfile,
            FellowshipProfileMain mainProfile,
            UserProfile userProfile
    ) {
        if (legacyProfile != null && legacyProfile.getAvatarUrl() != null && !legacyProfile.getAvatarUrl().isBlank()) {
            return legacyProfile.getAvatarUrl().trim();
        }
        if (mainProfile != null && mainProfile.getAvatar() != null && !mainProfile.getAvatar().isBlank()) {
            return mainProfile.getAvatar().trim();
        }
        if (userProfile != null && userProfile.getAvatar() != null && !userProfile.getAvatar().isBlank()) {
            return userProfile.getAvatar().trim();
        }
        if (user.getProfilePhoto() != null && !user.getProfilePhoto().isBlank()) {
            return user.getProfilePhoto().trim();
        }
        return "";
    }

    private Integer parseGenderCodeFromText(String rawGender) {
        String value = rawGender == null ? "" : rawGender.trim().toLowerCase();
        if (value.isEmpty()) {
            return null;
        }
        if (Set.of("male", "man", "m", "1", "boy", "男").contains(value)) {
            return 1;
        }
        if (Set.of("female", "woman", "f", "2", "girl", "女").contains(value)) {
            return 2;
        }
        return null;
    }

    /**
     * 管理端列表展示用年龄：优先按用户生日推算，其次联谊档案生日，最后用 users.age。
     */
    private Integer resolveAdminListAge(User user, FellowshipProfileMain mainProfile) {
        Integer fromUserBirth = ageFromLocalDateTime(user.getBirthDate());
        if (fromUserBirth != null) {
            return fromUserBirth;
        }
        if (mainProfile != null) {
            Integer fromMainBirth = ageFromLocalDate(mainProfile.getBirthday());
            if (fromMainBirth != null) {
                return fromMainBirth;
            }
        }
        Integer stored = user.getAge();
        if (stored != null && stored > 0 && stored < 150) {
            return stored;
        }
        return null;
    }

    private Integer ageFromLocalDateTime(LocalDateTime birth) {
        if (birth == null) {
            return null;
        }
        return safeYearsOld(birth.toLocalDate(), LocalDate.now());
    }

    private Integer ageFromLocalDate(LocalDate birth) {
        if (birth == null) {
            return null;
        }
        return safeYearsOld(birth, LocalDate.now());
    }

    private Integer safeYearsOld(LocalDate birth, LocalDate today) {
        if (birth.isAfter(today)) {
            return null;
        }
        int years = Period.between(birth, today).getYears();
        if (years < 0 || years >= 150) {
            return null;
        }
        return years;
    }

    /** 与 UnifiedProfileService 一致：取该用户最新一条 user_verifications，否则取 verification_requests。 */
    private static Map<Long, UserVerification> buildLatestUserVerificationByUserId(List<UserVerification> rows) {
        Map<Long, UserVerification> map = new HashMap<>();
        for (UserVerification v : rows) {
            if (v.getUserId() == null) {
                continue;
            }
            UserVerification cur = map.get(v.getUserId());
            if (cur == null || isSubmittedAtNewer(v.getSubmittedAt(), cur.getSubmittedAt())) {
                map.put(v.getUserId(), v);
            }
        }
        return map;
    }

    private static Map<Long, VerificationRequest> buildLatestVerificationRequestByUserId(List<VerificationRequest> rows) {
        Map<Long, VerificationRequest> map = new HashMap<>();
        for (VerificationRequest r : rows) {
            if (r.getUserId() == null) {
                continue;
            }
            VerificationRequest cur = map.get(r.getUserId());
            if (cur == null || isSubmittedAtNewer(r.getSubmittedAt(), cur.getSubmittedAt())) {
                map.put(r.getUserId(), r);
            }
        }
        return map;
    }

    private static boolean isSubmittedAtNewer(LocalDateTime candidate, LocalDateTime baseline) {
        if (candidate == null) {
            return false;
        }
        if (baseline == null) {
            return true;
        }
        return candidate.isAfter(baseline);
    }

    private static String resolveAdminListVerificationStatus(
            Long userId,
            Map<Long, UserVerification> latestUv,
            Map<Long, VerificationRequest> latestVr
    ) {
        UserVerification uv = latestUv.get(userId);
        if (uv != null && uv.getStatus() != null && !uv.getStatus().isBlank()) {
            return uv.getStatus();
        }
        VerificationRequest vr = latestVr.get(userId);
        if (vr != null && vr.getStatus() != null && !vr.getStatus().isBlank()) {
            return vr.getStatus();
        }
        return "none";
    }

    private Map<String, Object> toAdminUserPhotoView(UserPhoto photo) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", photo.getId());
        item.put("photoUrl", photo.getPhotoUrl() == null ? "" : photo.getPhotoUrl());
        item.put("status", photo.getStatus() == null ? "" : photo.getStatus());
        item.put("primary", Boolean.TRUE.equals(photo.getPrimary()));
        item.put("sortOrder", photo.getSortOrder() == null ? 0 : photo.getSortOrder());
        item.put("createdAt", photo.getCreatedAt());
        return item;
    }
}
