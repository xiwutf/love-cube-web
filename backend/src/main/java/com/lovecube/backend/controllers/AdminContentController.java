package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.Announcement;
import com.lovecube.backend.entity.Article;
import com.lovecube.backend.entity.PlatformEvent;
import com.lovecube.backend.entity.ReportRecord;
import com.lovecube.backend.entity.UserFeedback;
import com.lovecube.backend.entity.VerificationRequest;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.AnnouncementRepository;
import com.lovecube.backend.repository.ArticleRepository;
import com.lovecube.backend.repository.PlatformEventRepository;
import com.lovecube.backend.repository.ReportRecordRepository;
import com.lovecube.backend.repository.UserFeedbackRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.repository.VerificationRequestRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.FellowshipInviteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminContentController {
    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "ADMIN", "SUPER_ADMIN", "ROOT");
    private static final Set<String> ADMIN_MANAGEABLE_ROLES = Set.of("USER", "ADMIN");
    private static final Set<String> HIGH_PRIVILEGE_ROLES = Set.of("SUPER_ADMIN", "ROOT");

    private final AnnouncementRepository announcementRepository;
    private final ArticleRepository articleRepository;
    private final PlatformEventRepository platformEventRepository;
    private final UserRepository userRepository;
    private final VerificationRequestRepository verificationRequestRepository;
    private final ReportRecordRepository reportRecordRepository;
    private final UserFeedbackRepository userFeedbackRepository;
    private final AdminAuthService adminAuthService;
    private final FellowshipInviteService fellowshipInviteService;

    public AdminContentController(
            AnnouncementRepository announcementRepository,
            ArticleRepository articleRepository,
            PlatformEventRepository platformEventRepository,
            UserRepository userRepository,
            VerificationRequestRepository verificationRequestRepository,
            ReportRecordRepository reportRecordRepository,
            UserFeedbackRepository userFeedbackRepository,
            AdminAuthService adminAuthService,
            FellowshipInviteService fellowshipInviteService
    ) {
        this.announcementRepository = announcementRepository;
        this.articleRepository = articleRepository;
        this.platformEventRepository = platformEventRepository;
        this.userRepository = userRepository;
        this.verificationRequestRepository = verificationRequestRepository;
        this.reportRecordRepository = reportRecordRepository;
        this.userFeedbackRepository = userFeedbackRepository;
        this.adminAuthService = adminAuthService;
        this.fellowshipInviteService = fellowshipInviteService;
    }

    @GetMapping("/announcements")
    public List<Announcement> listAnnouncements(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        adminAuthService.requireAdmin(authHeader);
        return announcementRepository.findAll();
    }

    @PostMapping("/announcements")
    public Announcement saveAnnouncement(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Announcement payload
    ) {
        adminAuthService.requireAdmin(authHeader);
        if (payload.getId() == null || payload.getId().isBlank()) {
            payload.setId("announcement-" + UUID.randomUUID());
        }
        if (payload.getStatus() == null || payload.getStatus().isBlank()) {
            payload.setStatus("draft");
        }
        if (payload.getPublishDate() == null && "published".equals(payload.getStatus())) {
            payload.setPublishDate(LocalDateTime.now());
        }
        return announcementRepository.save(payload);
    }

    @GetMapping("/articles")
    public List<Article> listArticles(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        adminAuthService.requireAdmin(authHeader);
        return articleRepository.findAll();
    }

    @PostMapping("/articles")
    public Article saveArticle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Article payload
    ) {
        adminAuthService.requireAdmin(authHeader);
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
        adminAuthService.requireAdmin(authHeader);
        return platformEventRepository.findAll();
    }

    @PostMapping("/events")
    public PlatformEvent saveEvent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody PlatformEvent payload
    ) {
        adminAuthService.requireAdmin(authHeader);
        if (payload.getId() == null || payload.getId().isBlank()) {
            payload.setId("event-" + UUID.randomUUID());
        }
        if (payload.getStatus() == null || payload.getStatus().isBlank()) {
            payload.setStatus("draft");
        }
        if (payload.getSignupCount() == null) {
            payload.setSignupCount(0);
        }
        return platformEventRepository.save(payload);
    }

    @GetMapping("/users")
    public List<Map<String, Object>> listUsers(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User operator = adminAuthService.requireUser(authHeader);
        if (!adminAuthService.isAdmin(operator)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无管理员权限");
        }
        boolean hiddenSuperAdminOperator = adminAuthService.isHiddenSuperAdmin(operator);

        return userRepository.findAll().stream()
                .filter(user -> hiddenSuperAdminOperator || !adminAuthService.isHiddenSuperAdmin(user))
                .map(user -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("userId", user.getUserid());
                    item.put("username", user.getUsername() == null ? "" : user.getUsername());
                    item.put("phone", user.getPhoneNumber() == null ? "" : user.getPhoneNumber());
                    item.put("role", normalizeRoleForView(user.getRole(), adminAuthService.isAdmin(user)));
                    item.put("status", "DISABLED".equalsIgnoreCase(user.getUserStatus()) ? "disabled" : "active");
                    item.put("verificationStatus", "none");
                    item.put("inviteCode", user.getInviteCode() == null ? "" : user.getInviteCode());
                    item.put("invitedByUserId", user.getInvitedByUserId());
                    item.put("createdAt", user.getCreatedAt());
                    item.put("canForceDelete", hiddenSuperAdminOperator
                            && !operator.getUserid().equals(user.getUserid())
                            && !adminAuthService.isHiddenSuperAdmin(user));
                    return item;
                }).collect(Collectors.toList());
    }

    @PutMapping("/users/{userId}/role")
    public Map<String, Object> updateUserRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId,
            @RequestBody Map<String, Object> payload
    ) {
        User operator = adminAuthService.requireUser(authHeader);
        if (!adminAuthService.isAdmin(operator)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无管理员权限");
        }

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
        adminAuthService.requireAdmin(authHeader);
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
    public List<VerificationRequest> listVerifications(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        adminAuthService.requireAdmin(authHeader);
        return verificationRequestRepository.findAll();
    }

    @PostMapping("/verifications")
    public VerificationRequest saveVerification(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody VerificationRequest payload
    ) {
        adminAuthService.requireAdmin(authHeader);
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
        adminAuthService.requireAdmin(authHeader);
        return reportRecordRepository.findAll();
    }

    @GetMapping("/feedbacks")
    public List<UserFeedback> listFeedbacks(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        adminAuthService.requireAdmin(authHeader);
        return userFeedbackRepository.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping("/reports")
    public ReportRecord saveReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ReportRecord payload
    ) {
        adminAuthService.requireAdmin(authHeader);
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
        adminAuthService.requireAdmin(authHeader);
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

    @PatchMapping("/feedbacks/{id}")
    public UserFeedback updateFeedback(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestBody Map<String, Object> payload
    ) {
        adminAuthService.requireAdmin(authHeader);
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
        adminAuthService.requireAdmin(authHeader);
        announcementRepository.deleteById(id);
        return Map.of("message", "公告已删除", "id", id);
    }

    @DeleteMapping("/articles/{id}")
    public Map<String, Object> deleteArticle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        adminAuthService.requireAdmin(authHeader);
        articleRepository.deleteById(id);
        return Map.of("message", "资讯已删除", "id", id);
    }

    @DeleteMapping("/events/{id}")
    public Map<String, Object> deleteEvent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        adminAuthService.requireAdmin(authHeader);
        platformEventRepository.deleteById(id);
        return Map.of("message", "活动已删除", "id", id);
    }

    @PatchMapping("/verifications/{id}/review")
    public VerificationRequest reviewVerification(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestBody Map<String, Object> payload
    ) {
        adminAuthService.requireAdmin(authHeader);
        VerificationRequest record = verificationRequestRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "认证记录不存在"));
        String action = String.valueOf(payload.getOrDefault("action", ""));
        if ("approve".equals(action)) {
            record.setStatus("approved");
            record.setReviewedAt(LocalDateTime.now());
        } else if ("reject".equals(action)) {
            record.setStatus("rejected");
            record.setReviewedAt(LocalDateTime.now());
            if (payload.containsKey("reason")) {
                record.setRejectReason(String.valueOf(payload.get("reason")));
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "action 必须为 approve/reject");
        }
        return verificationRequestRepository.save(record);
    }

    @PutMapping("/users/{userId}/status")
    public Map<String, Object> updateUserStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId,
            @RequestBody Map<String, Object> payload
    ) {
        User operator = adminAuthService.requireUser(authHeader);
        if (!adminAuthService.isAdmin(operator)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无管理员权限");
        }

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

    @GetMapping("/stats")
    public Map<String, Object> getStats(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User operator = adminAuthService.requireUser(authHeader);
        if (!adminAuthService.isAdmin(operator)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无管理员权限");
        }
        boolean hiddenSuperAdminOperator = adminAuthService.isHiddenSuperAdmin(operator);

        long totalUsers = hiddenSuperAdminOperator
                ? userRepository.count()
                : userRepository.findAll().stream()
                .filter(user -> !adminAuthService.isHiddenSuperAdmin(user))
                .count();
        long totalAnnouncements = announcementRepository.count();
        long pendingVerifications = verificationRequestRepository.findAll().stream()
                .filter(v -> "pending".equals(v.getStatus())).count();
        long pendingReports = reportRecordRepository.findAll().stream()
                .filter(r -> !"resolved".equals(r.getStatus())).count();
        long pendingFeedbacks = userFeedbackRepository.countByStatusNot("resolved");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalAnnouncements", totalAnnouncements);
        stats.put("pendingVerifications", pendingVerifications);
        stats.put("pendingReports", pendingReports);
        stats.put("pendingFeedbacks", pendingFeedbacks);
        return stats;
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
}
