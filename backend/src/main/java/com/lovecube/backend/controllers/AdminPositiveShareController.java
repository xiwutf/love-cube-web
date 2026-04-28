package com.lovecube.backend.controllers;

import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.PositiveShareService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/admin/positive-shares")
public class AdminPositiveShareController {
    private final PositiveShareService positiveShareService;
    private final AdminAuthService adminAuthService;

    public AdminPositiveShareController(
            PositiveShareService positiveShareService,
            AdminAuthService adminAuthService
    ) {
        this.positiveShareService = positiveShareService;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping
    public Map<String, Object> listShares(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        adminAuthService.requireAdmin(authHeader);
        return positiveShareService.listSharesForAdmin(status, pageNum, pageSize);
    }

    @PatchMapping("/{id}/review")
    public Map<String, Object> reviewShare(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        adminAuthService.requireAdmin(authHeader);
        String status = String.valueOf(payload.getOrDefault("status", ""));
        return positiveShareService.reviewShare(id, status);
    }

    @PatchMapping("/batch-review")
    public Map<String, Object> batchReviewShares(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        adminAuthService.requireAdmin(authHeader);
        @SuppressWarnings("unchecked")
        List<Number> rawIds = (List<Number>) payload.get("ids");
        List<Long> ids = rawIds == null ? List.of() : rawIds.stream().map(Number::longValue).toList();
        String status = String.valueOf(payload.getOrDefault("status", ""));
        return positiveShareService.batchReviewShares(ids, status);
    }

    @GetMapping("/{id}/comments")
    public Map<String, Object> listComments(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        adminAuthService.requireAdmin(authHeader);
        return positiveShareService.listShareCommentsForAdmin(id, pageNum, pageSize);
    }
}
