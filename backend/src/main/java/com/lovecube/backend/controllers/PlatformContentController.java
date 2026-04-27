package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.Announcement;
import com.lovecube.backend.entity.Article;
import com.lovecube.backend.entity.EventSignup;
import com.lovecube.backend.entity.PlatformEvent;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.AnnouncementRepository;
import com.lovecube.backend.repository.ArticleRepository;
import com.lovecube.backend.repository.EventSignupRepository;
import com.lovecube.backend.repository.PlatformEventRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.HomeConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PlatformContentController {
    private final AnnouncementRepository announcementRepository;
    private final ArticleRepository articleRepository;
    private final PlatformEventRepository platformEventRepository;
    private final EventSignupRepository eventSignupRepository;
    private final HomeConfigService homeConfigService;
    private final AdminAuthService adminAuthService;

    public PlatformContentController(
            AnnouncementRepository announcementRepository,
            ArticleRepository articleRepository,
            PlatformEventRepository platformEventRepository,
            EventSignupRepository eventSignupRepository,
            HomeConfigService homeConfigService,
            AdminAuthService adminAuthService
    ) {
        this.announcementRepository = announcementRepository;
        this.articleRepository = articleRepository;
        this.platformEventRepository = platformEventRepository;
        this.eventSignupRepository = eventSignupRepository;
        this.homeConfigService = homeConfigService;
        this.adminAuthService = adminAuthService;
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
    public Announcement getAnnouncement(@PathVariable String id) {
        Announcement item = announcementRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "公告不存在"));
        item.setViewCount((item.getViewCount() == null ? 0 : item.getViewCount()) + 1);
        announcementRepository.save(item);
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
    public Article getArticle(@PathVariable String id) {
        Article item = articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "资讯不存在"));
        item.setViewCount((item.getViewCount() == null ? 0 : item.getViewCount()) + 1);
        articleRepository.save(item);
        return item;
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
    public PlatformEvent getEvent(@PathVariable String id) {
        PlatformEvent item = platformEventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        item.setViewCount((item.getViewCount() == null ? 0 : item.getViewCount()) + 1);
        platformEventRepository.save(item);
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
}
