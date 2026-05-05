package com.lovecube.backend.controllers;

import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.HelpMutualService;
import com.lovecube.backend.services.PermissionConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/help-requests")
public class AdminHelpRequestController {

    private final HelpMutualService helpMutualService;
    private final AdminAuthService adminAuthService;

    public AdminHelpRequestController(HelpMutualService helpMutualService, AdminAuthService adminAuthService) {
        this.helpMutualService = helpMutualService;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping
    public Map<String, Object> list(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.REVIEW_MANAGE);
        return helpMutualService.listForAdmin(status, pageNum, pageSize);
    }

    @PatchMapping("/{id}/review")
    public Map<String, Object> review(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable long id,
            @RequestBody Map<String, Object> payload
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.REVIEW_MANAGE);
        String status = String.valueOf(payload.getOrDefault("status", "")).trim().toLowerCase();
        return helpMutualService.adminReview(id, status);
    }

    @PostMapping("/{id}/review")
    public Map<String, Object> reviewPost(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable long id,
            @RequestBody Map<String, Object> payload
    ) {
        return review(authHeader, id, payload);
    }
}
