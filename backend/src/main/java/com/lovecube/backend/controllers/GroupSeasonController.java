package com.lovecube.backend.controllers;

import com.lovecube.backend.services.GroupSeasonService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/groups/season")
public class GroupSeasonController {

    private final GroupSeasonService groupSeasonService;

    public GroupSeasonController(GroupSeasonService groupSeasonService) {
        this.groupSeasonService = groupSeasonService;
    }

    @GetMapping("/rankings")
    public Map<String, Object> rankings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return groupSeasonService.getSeasonRankings(page, size);
    }

    @GetMapping("/{groupId}/rank")
    public Map<String, Object> groupRank(@PathVariable Long groupId) {
        return groupSeasonService.getGroupSeasonRank(groupId);
    }
}
