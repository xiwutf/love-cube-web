package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.Announcement;
import com.lovecube.backend.entity.Article;
import com.lovecube.backend.entity.PlatformEvent;
import com.lovecube.backend.entity.ReportRecord;
import com.lovecube.backend.entity.VerificationRequest;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.AnnouncementRepository;
import com.lovecube.backend.repository.ArticleRepository;
import com.lovecube.backend.repository.PlatformEventRepository;
import com.lovecube.backend.repository.ReportRecordRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.repository.VerificationRequestRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.FellowshipInviteService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminContentController {
    private final AnnouncementRepository announcementRepository;
    private final ArticleRepository articleRepository;
    private final PlatformEventRepository platformEventRepository;
    private final UserRepository userRepository;
    private final VerificationRequestRepository verificationRequestRepository;
    private final ReportRecordRepository reportRecordRepository;
    private final AdminAuthService adminAuthService;
    private final FellowshipInviteService fellowshipInviteService;

    public AdminContentController(
            AnnouncementRepository announcementRepository,
            ArticleRepository articleRepository,
            PlatformEventRepository platformEventRepository,
            UserRepository userRepository,
            VerificationRequestRepository verificationRequestRepository,
            ReportRecordRepository reportRecordRepository,
            AdminAuthService adminAuthService,
            FellowshipInviteService fellowshipInviteService
    ) {
        this.announcementRepository = announcementRepository;
        this.articleRepository = articleRepository;
        this.platformEventRepository = platformEventRepository;
        this.userRepository = userRepository;
        this.verificationRequestRepository = verificationRequestRepository;
        this.reportRecordRepository = reportRecordRepository;
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
        adminAuthService.requireAdmin(authHeader);
        return userRepository.findAll().stream().map(user -> {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", user.getUserid());
            item.put("username", user.getUsername() == null ? "" : user.getUsername());
            item.put("phone", user.getPhoneNumber() == null ? "" : user.getPhoneNumber());
            item.put("role", adminAuthService.isAdmin(user) ? "admin" : "user");
            item.put("status", "DISABLED".equalsIgnoreCase(user.getUserStatus()) ? "disabled" : "active");
            item.put("verificationStatus", "none");
            item.put("inviteCode", user.getInviteCode() == null ? "" : user.getInviteCode());
            item.put("invitedByUserId", user.getInvitedByUserId());
            item.put("createdAt", user.getCreatedAt());
            return item;
        }).collect(Collectors.toList());
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
}
