package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.InterestTopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interest-topics")
public class InterestTopicController {

    private final InterestTopicService topicService;
    private final AdminAuthService adminAuthService;

    public InterestTopicController(InterestTopicService topicService, AdminAuthService adminAuthService) {
        this.topicService = topicService;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping
    public List<Map<String, Object>> listTopics() {
        return topicService.listTopics();
    }

    @GetMapping("/{id}")
    public Map<String, Object> topicDetail(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        return topicService.getTopicDetail(id, page, size);
    }

    @PostMapping("/{id}/posts")
    public ResponseEntity<Map<String, Object>> createPost(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        String content = String.valueOf(body.getOrDefault("content", ""));
        return ResponseEntity.ok(topicService.createPost(user, id, content));
    }
}
