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
import org.springframework.web.bind.annotation.*;

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

    public AdminContentController(
            AnnouncementRepository announcementRepository,
            ArticleRepository articleRepository,
            PlatformEventRepository platformEventRepository,
            UserRepository userRepository,
            VerificationRequestRepository verificationRequestRepository,
            ReportRecordRepository reportRecordRepository,
            AdminAuthService adminAuthService
    ) {
        this.announcementRepository = announcementRepository;
        this.articleRepository = articleRepository;
        this.platformEventRepository = platformEventRepository;
        this.userRepository = userRepository;
        this.verificationRequestRepository = verificationRequestRepository;
        this.reportRecordRepository = reportRecordRepository;
        this.adminAuthService = adminAuthService;
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
            item.put("status", "active");
            item.put("verificationStatus", "none");
            item.put("createdAt", user.getCreatedAt());
            return item;
        }).collect(Collectors.toList());
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
}
