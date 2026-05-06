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

    @PostMapping("/clues")
    public Map<String, Object> submitClue(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        User user = adminAuthService.requireUser(authHeader);
        String title = String.valueOf(payload.getOrDefault("title", "")).trim();
        String type = String.valueOf(payload.getOrDefault("type", "")).trim();
        String contact = String.valueOf(payload.getOrDefault("contact", "")).trim();
        String location = String.valueOf(payload.getOrDefault("location", "")).trim();
        String summary = String.valueOf(payload.getOrDefault("summary", "")).trim();

        if (title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "资源标题不能为空");
        }
        if (type.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "资源分类不能为空");
        }
        if (contact.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "联系方式不能为空");
        }

        String summaryBody = summary.isBlank() ? "" : summary.trim();
        String contactLine = "联系方式：" + contact.trim();
        String combinedSummary;
        if (summaryBody.isEmpty()) {
            combinedSummary = "用户提交资源线索\n" + contactLine;
        } else {
            combinedSummary = summaryBody + "\n" + contactLine;
        }
        if (combinedSummary.length() > 500) {
            combinedSummary = combinedSummary.substring(0, 500);
        }

        LocalResource item = new LocalResource();
        item.setTitle(title);
        item.setType(type);
        item.setLocation(location.isBlank() ? null : location);
        item.setSummary(combinedSummary);
        item.setCoverUrl(null);
        item.setHeat(0);
        item.setInterestCount(0);
        item.setStatus("pending");
        item.setCreatedBy(user.getUserid());
        item.setEventTime(null);
        LocalResource saved = localResourceRepository.save(item);
        return Map.of(
                "id", saved.getId(),
                "status", saved.getStatus(),
                "message", "线索已提交，待运营处理"
        );
    }
}
