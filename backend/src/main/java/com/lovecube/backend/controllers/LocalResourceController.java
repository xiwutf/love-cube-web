package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.LocalResource;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.LocalResourceRepository;
import com.lovecube.backend.services.AdminAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/local-resources")
public class LocalResourceController {
    private final LocalResourceRepository localResourceRepository;
    private final AdminAuthService adminAuthService;

    public LocalResourceController(LocalResourceRepository localResourceRepository, AdminAuthService adminAuthService) {
        this.localResourceRepository = localResourceRepository;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping
    public List<LocalResource> listPublished(@RequestParam(required = false) String type) {
        List<LocalResource> items = localResourceRepository.findByStatusOrderByHeatDescUpdatedAtDesc("published");
        if (type == null || type.isBlank() || "all".equalsIgnoreCase(type)) return items;
        return items.stream().filter(item -> type.equalsIgnoreCase(item.getType())).toList();
    }

    @GetMapping("/{id}")
    public LocalResource detail(@PathVariable Long id) {
        LocalResource item = localResourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "本地资源不存在"));
        if (!"published".equalsIgnoreCase(item.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "本地资源不存在");
        }
        return item;
    }

    @PostMapping("/{id}/interest")
    @Transactional
    public Map<String, Object> interest(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        LocalResource item = localResourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "本地资源不存在"));
        if (!"published".equalsIgnoreCase(item.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该资源暂不可操作");
        }
        item.setInterestCount((item.getInterestCount() == null ? 0 : item.getInterestCount()) + 1);
        item.setHeat((item.getHeat() == null ? 0 : item.getHeat()) + 1);
        localResourceRepository.save(item);
        return Map.of(
                "resourceId", item.getId(),
                "interestCount", item.getInterestCount(),
                "heat", item.getHeat(),
                "userId", user.getUserid(),
                "message", "已标记感兴趣"
        );
    }
}
