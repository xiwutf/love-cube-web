package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.Announcement;
import com.lovecube.backend.entity.Article;
import com.lovecube.backend.entity.EventSignup;
import com.lovecube.backend.entity.PlatformEvent;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.AnnouncementRepository;
import com.lovecube.backend.repository.ArticleRepository;
import com.lovecube.backend.repository.DynamicRepository;
import com.lovecube.backend.repository.EventSignupRepository;
import com.lovecube.backend.repository.HomeConfigRepository;
import com.lovecube.backend.repository.PlatformEventRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.GrowthService;
import com.lovecube.backend.services.HomeConfigService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class PlatformContentController {
    private final AnnouncementRepository announcementRepository;
    private final ArticleRepository articleRepository;
    private final PlatformEventRepository platformEventRepository;
    private final EventSignupRepository eventSignupRepository;
    private final HomeConfigService homeConfigService;
    private final AdminAuthService adminAuthService;
    private final UserRepository userRepository;
    private final HomeConfigRepository homeConfigRepository;
    private final DynamicRepository dynamicRepository;
    private final GrowthService growthService;

    public PlatformContentController(
            AnnouncementRepository announcementRepository,
            ArticleRepository articleRepository,
            PlatformEventRepository platformEventRepository,
            EventSignupRepository eventSignupRepository,
            HomeConfigService homeConfigService,
            AdminAuthService adminAuthService,
            UserRepository userRepository,
            HomeConfigRepository homeConfigRepository,
            DynamicRepository dynamicRepository,
            GrowthService growthService
    ) {
        this.announcementRepository = announcementRepository;
        this.articleRepository = articleRepository;
        this.platformEventRepository = platformEventRepository;
        this.eventSignupRepository = eventSignupRepository;
        this.homeConfigService = homeConfigService;
        this.adminAuthService = adminAuthService;
        this.userRepository = userRepository;
        this.homeConfigRepository = homeConfigRepository;
        this.dynamicRepository = dynamicRepository;
        this.growthService = growthService;
    }

    @GetMapping("/announcements")
    public List<Announcement> listAnnouncements(
            @RequestParam(defaultValue = "published") String status,
            @RequestParam(required = false) String category
    ) {
        List<Announcement> items = announcementRepository.findByStatusPinnedFirst(status);
        if (category != null && !category.isBlank()) {
            items = items.stream().filter(a -> category.equals(a.getCategory())).toList();
        }
        return items;
    }

    @GetMapping("/announcements/{id}")
    public Announcement getAnnouncement(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Announcement item = announcementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "公告不存在"));
        item.setViewCount((item.getViewCount() == null ? 0 : item.getViewCount()) + 1);
        announcementRepository.save(item);
        tryRecordViewAction(authHeader, "ANNOUNCEMENT_" + id);
        return item;
    }

    @GetMapping("/articles")
    public List<Article> listArticles(
            @RequestParam(defaultValue = "published") String status,
            @RequestParam(required = false) String category
    ) {
        List<Article> items = articleRepository.findByStatusPinnedFirst(status);
        if (category != null && !category.isBlank()) {
            items = items.stream().filter(a -> category.equals(a.getCategory())).toList();
        }
        return items;
    }

    @GetMapping("/articles/{id}")
    public Article getArticle(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Article item = articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "资讯不存在"));
        item.setViewCount((item.getViewCount() == null ? 0 : item.getViewCount()) + 1);
        articleRepository.save(item);
        tryRecordViewAction(authHeader, "ARTICLE_" + id);
        return item;
    }

    @PostMapping("/articles/submissions")
    public Map<String, Object> submitArticle(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        User user = adminAuthService.requireUser(authHeader);
        String title = String.valueOf(payload.getOrDefault("title", "")).trim();
        String summary = String.valueOf(payload.getOrDefault("summary", "")).trim();
        String content = String.valueOf(payload.getOrDefault("content", "")).trim();
        String category = String.valueOf(payload.getOrDefault("category", "平台资讯")).trim();
        String tag = String.valueOf(payload.getOrDefault("tag", category)).trim();
        String coverUrl = String.valueOf(payload.getOrDefault("coverUrl", "")).trim();

        if (title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "标题不能为空");
        }
        if (content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "正文不能为空");
        }

        Article article = new Article();
        article.setId("article-" + UUID.randomUUID());
        article.setTitle(title);
        article.setSummary(summary);
        article.setContent(content);
        article.setCategory(category.isBlank() ? "平台资讯" : category);
        article.setTag(tag.isBlank() ? article.getCategory() : tag);
        article.setCoverUrl(coverUrl);
        article.setStatus("draft");
        article.setPinned(false);
        article.setRecommended(false);
        article.setViewCount(0);
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        Article saved = articleRepository.save(article);
        growthService.recordAction(user.getUserid(), "POST_CONTENT", "ARTICLE_SUBMISSION_" + saved.getId());

        return Map.of(
                "id", saved.getId(),
                "title", saved.getTitle(),
                "status", saved.getStatus(),
                "message", "投稿已提交，待管理员审核发布"
        );
    }

    @GetMapping("/events")
    public List<PlatformEvent> listEvents(
            @RequestParam(defaultValue = "published") String status,
            @RequestParam(required = false) String category
    ) {
        List<PlatformEvent> items = platformEventRepository.findByStatusPinnedFirst(status);
        if (category != null && !category.isBlank()) {
            items = items.stream().filter(e -> category.equals(e.getCategory())).toList();
        }
        return items;
    }

    @GetMapping("/events/{id}")
    public PlatformEvent getEvent(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        PlatformEvent item = platformEventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        item.setViewCount((item.getViewCount() == null ? 0 : item.getViewCount()) + 1);
        platformEventRepository.save(item);
        tryRecordViewAction(authHeader, "EVENT_" + id);
        return item;
    }

    @PostMapping("/events/{id}/signup")
    @Transactional
    public Map<String, Object> signupEvent(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        PlatformEvent item = platformEventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        if (!"published".equals(item.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动暂不可报名");
        }

        boolean alreadySignedUp = eventSignupRepository.existsByEventIdAndUserId(id, user.getUserid());
        if (!alreadySignedUp) {
            EventSignup signup = new EventSignup();
            signup.setEventId(id);
            signup.setUserId(user.getUserid());
            eventSignupRepository.save(signup);
            item.setSignupCount((item.getSignupCount() == null ? 0 : item.getSignupCount()) + 1);
        }

        platformEventRepository.save(item);

        return Map.of(
                "signedUp", true,
                "alreadySignedUp", alreadySignedUp,
                "signupCount", item.getSignupCount() == null ? 0 : item.getSignupCount(),
                "message", alreadySignedUp ? "已报名，无需重复提交" : "报名成功"
        );
    }

    @GetMapping("/events/my-signups")
    public List<Map<String, Object>> getMyEventSignups(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        List<EventSignup> signups = eventSignupRepository.findByUserIdOrderByCreatedAtDesc(user.getUserid());
        List<String> eventIds = signups.stream().map(EventSignup::getEventId).toList();
        Map<String, PlatformEvent> eventMap = platformEventRepository.findAllById(eventIds).stream()
                .collect(java.util.stream.Collectors.toMap(PlatformEvent::getId, e -> e));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (EventSignup signup : signups) {
            PlatformEvent event = eventMap.get(signup.getEventId());
            if (event == null) {
                continue;
            }
            rows.add(Map.of(
                    "eventId", event.getId(),
                    "title", event.getTitle(),
                    "summary", event.getSummary() == null ? "" : event.getSummary(),
                    "location", event.getLocation() == null ? "" : event.getLocation(),
                    "eventTime", event.getEventTime(),
                    "signupAt", signup.getCreatedAt()
            ));
        }
        return rows;
    }

    @GetMapping("/platform/stats")
    public Map<String, Object> getPlatformStats() {
        long userCount = userRepository.count();
        long eventSignupCount = eventSignupRepository.count();
        long dynamicsCount = dynamicRepository.countByIsDeletedFalse();
        long articleViewCount = articleRepository.sumPublishedViewCount();
        long citiesCount = platformEventRepository.countDistinctPublishedLocations();
        return Map.of(
                "userCount", userCount,
                "eventSignupCount", eventSignupCount,
                "dynamicsCount", dynamicsCount,
                "articleViewCount", articleViewCount,
                "citiesCount", citiesCount
        );
    }

    @GetMapping("/articles/hot-topics")
    public List<Map<String, Object>> getHotTopics(
            @RequestParam(defaultValue = "5") int limit) {
        List<Object[]> rows = articleRepository.findHotTagsWithHeat(PageRequest.of(0, Math.min(limit, 20)));
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("rank", i + 1);
            item.put("name", row[0]);
            item.put("heatValue", row[1]);
            result.add(item);
        }
        return result;
    }

    @GetMapping("/users/recommended-authors")
    public List<Map<String, Object>> getRecommendedAuthors(
            @RequestParam(defaultValue = "4") int limit) {
        List<User> admins = userRepository.findByRoleInOrderByCreatedAtAsc(List.of("ADMIN", "SUPER_ADMIN", "ROOT"));
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : admins.subList(0, Math.min(limit, admins.size()))) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", u.getUserid());
            item.put("username", u.getUsername());
            item.put("badge", "官方");
            item.put("description", u.getBio() != null ? u.getBio() : "Love Cube 官方内容团队");
            item.put("avatarUrl", u.getProfilePhoto());
            result.add(item);
        }
        return result;
    }

    @GetMapping("/events/stats")
    public Map<String, Object> getEventsStats() {
        long totalEvents = platformEventRepository.countByStatus("published");
        long totalSignups = eventSignupRepository.count();
        long citiesCount = platformEventRepository.countDistinctPublishedLocations();
        long activeEvents = platformEventRepository.countByStatus("published");
        return Map.of(
                "totalEvents", totalEvents,
                "totalSignups", totalSignups,
                "citiesCount", citiesCount,
                "activeEvents", activeEvents
        );
    }

    @GetMapping("/modules/stats")
    public Map<String, Object> getModulesStats() {
        long totalModules = homeConfigRepository.countByConfigGroup("module");
        long activeModules = homeConfigRepository.countByConfigGroupAndEnabledTrue("module");
        return Map.of(
                "totalModules", totalModules,
                "activeModules", activeModules
        );
    }

    @GetMapping("/home/config")
    public Map<String, Object> getHomeConfig() {
        return homeConfigService.getPublicHomeConfig();
    }

    @GetMapping("/admin/reserved")
    public Map<String, Object> adminReserved() {
        return Map.of(
                "announcements", "/api/admin/announcements",
                "articles", "/api/admin/articles",
                "events", "/api/admin/events",
                "users", "/api/admin/users",
                "verifications", "/api/admin/verifications",
                "reports", "/api/admin/reports"
        );
    }

    private void tryRecordViewAction(String authHeader, String bizId) {
        if (authHeader == null || authHeader.isBlank()) {
            return;
        }
        try {
            User user = adminAuthService.requireUser(authHeader);
            growthService.recordAction(user.getUserid(), "VIEW_CONTENT", bizId);
        } catch (Exception ignored) {
        }
    }
}
