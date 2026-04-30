package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.PlatGroup;
import com.lovecube.backend.entity.PlatGroupMember;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.PlatGroupMemberRepository;
import com.lovecube.backend.repository.PlatGroupRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.PlatformGroupSupport;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/platform/me")
public class PlatformMeController {

    private final PlatGroupRepository groupRepository;
    private final PlatGroupMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final AdminAuthService adminAuthService;

    public PlatformMeController(
            PlatGroupRepository groupRepository,
            PlatGroupMemberRepository memberRepository,
            UserRepository userRepository,
            AdminAuthService adminAuthService) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping("/groups")
    public Map<String, Object> myGroupsBucketed(@RequestHeader("Authorization") String authHeader) {
        User user = adminAuthService.requireUser(authHeader);
        List<PlatGroupMember> memberships = memberRepository.findByUserId(user.getUserid());

        List<Map<String, Object>> createdGroups = new ArrayList<>();
        List<Map<String, Object>> managedGroups = new ArrayList<>();
        List<Map<String, Object>> joinedGroups = new ArrayList<>();

        Set<Long> groupIds = memberships.stream()
                .filter(m -> "approved".equals(m.getStatus()) || "pending".equals(m.getStatus()))
                .map(PlatGroupMember::getGroupId)
                .collect(Collectors.toSet());

        if (groupIds.isEmpty()) {
            return Map.of(
                    "createdGroups", createdGroups,
                    "managedGroups", managedGroups,
                    "joinedGroups", joinedGroups
            );
        }

        Map<Long, PlatGroup> groupMap = groupRepository.findAllById(groupIds).stream()
                .filter(g -> "published".equals(g.getStatus()))
                .collect(Collectors.toMap(PlatGroup::getId, g -> g));

        Set<Long> ownerIds = groupMap.values().stream()
                .map(PlatGroup::getOwnerUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> ownerUsers = userRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));

        for (PlatGroupMember m : memberships) {
            PlatGroup g = groupMap.get(m.getGroupId());
            if (g == null) continue;
            if (!("approved".equals(m.getStatus()) || "pending".equals(m.getStatus()))) continue;

            Map<Long, PlatGroupMember> single = Collections.singletonMap(g.getId(), m);
            Map<String, Object> summary = PlatformGroupSupport.buildGroupSummary(g, single, ownerUsers);

            if ("owner".equals(m.getRole())) {
                createdGroups.add(summary);
            } else if ("admin".equals(m.getRole()) && "approved".equals(m.getStatus())) {
                managedGroups.add(summary);
            } else if ("member".equals(m.getRole())) {
                joinedGroups.add(summary);
            }
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("createdGroups", createdGroups);
        out.put("managedGroups", managedGroups);
        out.put("joinedGroups", joinedGroups);
        return out;
    }
}
