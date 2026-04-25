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
    public List<Announcement> listAnnouncements(@RequestParam(defaultValue = "published") String status) {
        return announcementRepository.findByStatusOrderByPublishDateDesc(status);
    }

    @GetMapping("/announcements/{id}")
    public Announcement getAnnouncement(@PathVariable String id) {
        return announcementRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "公告不存在"));
    }

    @GetMapping("/articles")
    public List<Article> listArticles(@RequestParam(defaultValue = "published") String status) {
        return articleRepository.findByStatusOrderByPublishDateDesc(status);
    }

    @GetMapping("/articles/{id}")
    public Article getArticle(@PathVariable String id) {
        return articleRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "资讯不存在"));
    }

    @GetMapping("/events")
    public List<PlatformEvent> listEvents(@RequestParam(defaultValue = "published") String status) {
        return platformEventRepository.findByStatusOrderByEventTimeDesc(status);
    }

    @GetMapping("/events/{id}")
    public PlatformEvent getEvent(@PathVariable String id) {
        return platformEventRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
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
