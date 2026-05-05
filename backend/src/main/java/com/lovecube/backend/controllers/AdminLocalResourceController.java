package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.LocalResource;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.LocalResourceRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.PermissionConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/local-resources")
public class AdminLocalResourceController {
    private final LocalResourceRepository localResourceRepository;
    private final AdminAuthService adminAuthService;

    public AdminLocalResourceController(LocalResourceRepository localResourceRepository, AdminAuthService adminAuthService) {
        this.localResourceRepository = localResourceRepository;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping
    public List<LocalResource> list(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_MANAGE);
        return localResourceRepository.findAll().stream()
                .sorted((a, b) -> {
                    if (a.getUpdatedAt() == null && b.getUpdatedAt() == null) return 0;
                    if (a.getUpdatedAt() == null) return 1;
                    if (b.getUpdatedAt() == null) return -1;
                    return b.getUpdatedAt().compareTo(a.getUpdatedAt());
                })
                .toList();
    }

    @PostMapping
    public LocalResource create(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody LocalResource payload
    ) {
        User admin = adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_MANAGE);
        payload.setId(null);
        if (payload.getStatus() == null || payload.getStatus().isBlank()) payload.setStatus("draft");
        if (payload.getHeat() == null) payload.setHeat(0);
        if (payload.getInterestCount() == null) payload.setInterestCount(0);
        payload.setCreatedBy(admin.getUserid());
        return localResourceRepository.save(payload);
    }

    @PutMapping("/{id}")
    public LocalResource update(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody LocalResource payload
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_MANAGE);
        LocalResource current = localResourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "本地资源不存在"));
        current.setTitle(payload.getTitle());
        current.setType(payload.getType());
        current.setLocation(payload.getLocation());
        current.setEventTime(payload.getEventTime());
        current.setSummary(payload.getSummary());
        current.setCoverUrl(payload.getCoverUrl());
        current.setHeat(payload.getHeat() == null ? current.getHeat() : payload.getHeat());
        current.setInterestCount(payload.getInterestCount() == null ? current.getInterestCount() : payload.getInterestCount());
        if (payload.getStatus() != null && !payload.getStatus().isBlank()) current.setStatus(payload.getStatus());
        return localResourceRepository.save(current);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_MANAGE);
        localResourceRepository.deleteById(id);
        return Map.of("id", id, "message", "本地资源已删除");
    }

    @PostMapping("/{id}/offline")
    public LocalResource offline(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_MANAGE);
        LocalResource item = localResourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "本地资源不存在"));
        item.setStatus("offline");
        return localResourceRepository.save(item);
    }

    @PostMapping("/{id}/publish")
    public LocalResource publish(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.CONTENT_MANAGE);
        LocalResource item = localResourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "本地资源不存在"));
        item.setStatus("published");
        return localResourceRepository.save(item);
    }
}
