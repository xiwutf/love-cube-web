package com.lovecube.backend.controllers;

import com.lovecube.backend.dto.UserFilterDTO;
import com.lovecube.backend.entity.Banner;
import com.lovecube.backend.models.User;
import com.lovecube.backend.services.HomeService;
import com.lovecube.backend.services.UnifiedProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    @Autowired
    private HomeService homeService;

    @Autowired
    private UnifiedProfileService unifiedProfileService;

    @GetMapping("/home/init")
    public ResponseEntity<Map<String, Object>> getHomeInit(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(homeService.getHomeInit(authHeader));
    }

    @GetMapping("/banners")
    public ResponseEntity<List<Banner>> getBanners() {
        return ResponseEntity.ok(homeService.getBanners());
    }

    @GetMapping("/recommends")
    public ResponseEntity<List<Map<String, Object>>> getRecommends(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(homeService.getRecommends(resolveViewerUserId(authHeader)));
    }

    @GetMapping("/newcomers")
    public ResponseEntity<List<Map<String, Object>>> getNewcomers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(homeService.getNewcomers(resolveViewerUserId(authHeader)));
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(homeService.searchUsers(keyword, page, size));
    }

    @PostMapping("/filter")
    public ResponseEntity<List<User>> filterUsers(@RequestBody UserFilterDTO filterDTO) {
        return ResponseEntity.ok(homeService.filterUsers(filterDTO));
    }

    private Long resolveViewerUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            return user != null ? user.getUserid() : null;
        } catch (Exception ignored) {
            return null;
        }
    }
} 