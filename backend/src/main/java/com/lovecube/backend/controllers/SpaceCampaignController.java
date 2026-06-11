package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.SpaceCampaignService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/platform/groups/{groupId}/space-campaigns")
public class SpaceCampaignController {

    private final AdminAuthService adminAuthService;
    private final SpaceCampaignService spaceCampaignService;

    public SpaceCampaignController(
            AdminAuthService adminAuthService,
            SpaceCampaignService spaceCampaignService
    ) {
        this.adminAuthService = adminAuthService;
        this.spaceCampaignService = spaceCampaignService;
    }

    @GetMapping
    public List<Map<String, Object>> list(
            @PathVariable Long groupId,
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return spaceCampaignService.listForGroup(groupId, user);
    }

    @GetMapping("/active")
    public Map<String, Object> active(
            @PathVariable Long groupId,
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return spaceCampaignService.getActive(groupId, user);
    }

    @GetMapping("/{campaignId}")
    public Map<String, Object> detail(
            @PathVariable Long groupId,
            @PathVariable Long campaignId,
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return spaceCampaignService.getDetail(groupId, campaignId, user);
    }

    @PostMapping
    public Map<String, Object> create(
            @PathVariable Long groupId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return spaceCampaignService.create(groupId, user, body);
    }

    @PostMapping("/{campaignId}/days/{dayNumber}/complete")
    public Map<String, Object> completeDay(
            @PathVariable Long groupId,
            @PathVariable Long campaignId,
            @PathVariable Integer dayNumber,
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return spaceCampaignService.completeDay(groupId, campaignId, user, dayNumber);
    }

    @PostMapping("/{campaignId}/notify")
    public Map<String, Object> notifyMembers(
            @PathVariable Long groupId,
            @PathVariable Long campaignId,
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return spaceCampaignService.notifyMembers(groupId, campaignId, user);
    }
}
