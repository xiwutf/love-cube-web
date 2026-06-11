package com.lovecube.backend.controllers;

import com.lovecube.backend.growth.GrowthCampaignTemplate;
import com.lovecube.backend.models.User;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.GrowthCampaignService;
import com.lovecube.backend.services.PermissionConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/growth")
public class AdminGrowthCampaignController {
    private final AdminAuthService adminAuthService;
    private final GrowthCampaignService growthCampaignService;

    public AdminGrowthCampaignController(
            AdminAuthService adminAuthService,
            GrowthCampaignService growthCampaignService
    ) {
        this.adminAuthService = adminAuthService;
        this.growthCampaignService = growthCampaignService;
    }

    @GetMapping("/campaign/preview")
    public Map<String, Object> previewSegment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam String segment
    ) {
        User operator = adminAuthService.requirePermission(authHeader, PermissionConstants.USER_MANAGE);
        return growthCampaignService.previewSegment(operator, segment);
    }

    @PostMapping("/campaign/send")
    public ResponseEntity<Map<String, Object>> sendCampaign(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody SendCampaignRequest body
    ) {
        User operator = adminAuthService.requirePermission(authHeader, PermissionConstants.USER_MANAGE);
        Map<String, Object> result = growthCampaignService.createAndSend(
                operator,
                body.segment(),
                body.templateCode(),
                body.channel(),
                body.name()
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/campaigns")
    public List<Map<String, Object>> listCampaigns(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.USER_MANAGE);
        return growthCampaignService.listCampaigns();
    }

    @GetMapping("/campaigns/{id}")
    public Map<String, Object> campaignDetail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.USER_MANAGE);
        return growthCampaignService.getCampaignDetail(id, page, size);
    }

    @PostMapping("/campaigns/{id}/refresh-conversion")
    public Map<String, Object> refreshConversion(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.USER_MANAGE);
        return growthCampaignService.refreshConversion(id);
    }

    @GetMapping("/campaign/templates")
    public List<Map<String, Object>> listTemplates(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.USER_MANAGE);
        return java.util.Arrays.stream(GrowthCampaignTemplate.values())
                .map(template -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("code", template.getCode());
                    row.put("title", template.getTitle());
                    row.put("content", template.getContent());
                    row.put("actionUrl", template.getActionUrl());
                    return row;
                })
                .collect(Collectors.toList());
    }

    public record SendCampaignRequest(
            String segment,
            String templateCode,
            String channel,
            String name
    ) {
    }
}
