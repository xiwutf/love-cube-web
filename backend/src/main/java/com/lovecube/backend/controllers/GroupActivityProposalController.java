package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.GroupActivityProposalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/platform/groups/{groupId}")
public class GroupActivityProposalController {

    private final AdminAuthService adminAuthService;
    private final GroupActivityProposalService proposalService;

    public GroupActivityProposalController(
            AdminAuthService adminAuthService,
            GroupActivityProposalService proposalService
    ) {
        this.adminAuthService = adminAuthService;
        this.proposalService = proposalService;
    }

    @PostMapping("/activity-proposals")
    public Map<String, Object> submit(
            @PathVariable Long groupId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return proposalService.submitProposal(groupId, user, body);
    }

    @GetMapping("/my-activity-proposals")
    public Map<String, Object> myProposals(
            @PathVariable Long groupId,
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return proposalService.listMyProposals(groupId, user);
    }

    @GetMapping("/activity-proposals/pending")
    public Map<String, Object> pending(
            @PathVariable Long groupId,
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return proposalService.listPending(groupId, user);
    }

    @PostMapping("/activity-proposals/{activityId}/review")
    public Map<String, Object> review(
            @PathVariable Long groupId,
            @PathVariable Long activityId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return proposalService.reviewProposal(groupId, activityId, user, body);
    }
}
