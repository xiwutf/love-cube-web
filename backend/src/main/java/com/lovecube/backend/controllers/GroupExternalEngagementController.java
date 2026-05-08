package com.lovecube.backend.controllers;

import com.lovecube.backend.services.GroupExternalEngagementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * platform_groups（字符串 ID）团体的打卡 / 每日任务 / 活动 API。
 * 数字 ID 的 plat 团体继续使用 {@link com.lovecube.backend.controllers.PlatformGroupController}。
 */
@RestController
@RequestMapping("/api/groups")
public class GroupExternalEngagementController {

    private final GroupExternalEngagementService engagementService;

    public GroupExternalEngagementController(GroupExternalEngagementService engagementService) {
        this.engagementService = engagementService;
    }

    @GetMapping("/{id}/checkins/summary")
    public Map<String, Object> checkinSummary(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return engagementService.checkinSummary(id, authHeader);
    }

    @GetMapping("/{id}/checkins/rankings")
    public Map<String, Object> checkinRankings(
            @PathVariable String id,
            @RequestParam(defaultValue = "daily") String type,
            @RequestHeader("Authorization") String authHeader) {
        return engagementService.checkinRankings(id, type, authHeader);
    }

    @PostMapping("/{id}/checkins")
    public Map<String, Object> createCheckin(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) Map<String, Object> payload) {
        return engagementService.createCheckin(id, authHeader, payload != null ? payload : Map.of());
    }

    @GetMapping("/{id}/tasks/today")
    public List<Map<String, Object>> todayTasks(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        return engagementService.todayTasks(id, authHeader);
    }

    @PostMapping("/{id}/tasks/{taskCode}/claim")
    public Map<String, Object> claimTask(
            @PathVariable String id,
            @PathVariable String taskCode,
            @RequestHeader("Authorization") String authHeader) {
        return engagementService.claimTask(id, taskCode, authHeader);
    }

    @GetMapping("/{id}/activities")
    public Map<String, Object> listActivities(
            @PathVariable String id,
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return engagementService.listActivities(id, filter, page, size, authHeader);
    }

    @PostMapping("/{id}/activities")
    public Map<String, Object> createActivity(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) Map<String, Object> payload) {
        return engagementService.createActivity(id, authHeader, payload != null ? payload : Map.of());
    }

    @GetMapping("/{id}/activities/{activityId}")
    public Map<String, Object> getActivity(
            @PathVariable String id,
            @PathVariable Long activityId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return engagementService.getActivity(id, activityId, authHeader);
    }

    @PostMapping("/{id}/activities/{activityId}/signup")
    public Map<String, Object> signUp(
            @PathVariable String id,
            @PathVariable Long activityId,
            @RequestHeader("Authorization") String authHeader) {
        return engagementService.signUpActivity(id, activityId, authHeader);
    }

    @PostMapping("/{id}/activities/{activityId}/cancel-signup")
    public Map<String, Object> cancelSignUp(
            @PathVariable String id,
            @PathVariable Long activityId,
            @RequestHeader("Authorization") String authHeader) {
        return engagementService.cancelActivitySignup(id, activityId, authHeader);
    }

    @PatchMapping("/{id}/activities/{activityId}")
    public Map<String, Object> updateActivity(
            @PathVariable String id,
            @PathVariable Long activityId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) Map<String, Object> payload) {
        return engagementService.updateActivity(id, activityId, authHeader,
                payload != null ? payload : Map.of());
    }
}
