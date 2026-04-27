package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.Announcement;
import com.lovecube.backend.entity.Article;
import com.lovecube.backend.entity.PlatformEvent;
import com.lovecube.backend.repository.AnnouncementRepository;
import com.lovecube.backend.repository.ArticleRepository;
import com.lovecube.backend.repository.PlatformEventRepository;
import org.springframework.http.HttpStatus;
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

    public PlatformContentController(
            AnnouncementRepository announcementRepository,
            ArticleRepository articleRepository,
            PlatformEventRepository platformEventRepository
    ) {
        this.announcementRepository = announcementRepository;
        this.articleRepository = articleRepository;
        this.platformEventRepository = platformEventRepository;
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
