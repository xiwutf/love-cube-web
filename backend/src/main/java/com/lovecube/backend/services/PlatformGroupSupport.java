package com.lovecube.backend.services;

import com.lovecube.backend.entity.PlatGroup;
import com.lovecube.backend.entity.PlatGroupMember;
import com.lovecube.backend.models.User;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PlatformGroupSupport {

    private static final Map<String, String> TYPE_LABELS = Map.of(
            "region", "地区团体",
            "church", "教会团体",
            "study", "学习小组",
            "interest", "兴趣团体",
            "family", "生活团契",
            "service", "事工团队"
    );

    private PlatformGroupSupport() {
    }

    public static String normalizeJoinModeForStore(Object rawObj) {
        if (rawObj == null) return "audit";
        String raw = String.valueOf(rawObj);
        if (raw.isBlank()) return "audit";
        String s = raw.trim().toLowerCase();
        if ("open".equals(s)) return "free";
        if ("free".equals(s)) return "free";
        if ("audit".equals(s)) return "audit";
        if ("invite".equals(s)) return "invite";
        return "audit";
    }

    /** API filter/query param: open | audit | invite — matches DB join_mode. */
    public static boolean matchesJoinModeFilter(String storedJoinMode, String apiJoinMode) {
        if (apiJoinMode == null || apiJoinMode.isBlank()) return true;
        String f = apiJoinMode.trim().toLowerCase();
        String stored = storedJoinMode == null ? "" : storedJoinMode.toLowerCase();
        return switch (f) {
            case "open" -> "free".equals(stored);
            case "audit" -> "audit".equals(stored);
            case "invite" -> "invite".equals(stored);
            default -> true;
        };
    }

    public static String joinModeToApiKey(String stored) {
        if ("free".equals(stored)) return "open";
        if ("audit".equals(stored)) return "audit";
        if ("invite".equals(stored)) return "invite";
        return stored != null ? stored : "audit";
    }

    public static Map<String, Object> buildGroupSummary(
            PlatGroup g,
            Map<Long, PlatGroupMember> memberMap,
            Map<Long, User> ownerUsers) {

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", g.getId());
        item.put("slug", g.getSlug());
        item.put("name", g.getName());
        item.put("coverUrl", g.getCoverUrl());
        item.put("type", g.getType());
        String typeName = TYPE_LABELS.getOrDefault(g.getType(), g.getType());
        item.put("typeName", typeName);
        item.put("category", typeName);
        item.put("region", g.getRegion());
        item.put("location", g.getRegion());
        item.put("memberCount", g.getMemberCount() == null ? 0 : g.getMemberCount());
        item.put("description", g.getDescription());
        item.put("tags", g.getTags());

        String jm = g.getJoinMode();
        item.put("joinMode", jm);
        item.put("joinModeKey", joinModeToApiKey(jm));
        item.put("joinType", "free".equals(jm) ? "open" : "approval");

        item.put("status", g.getStatus());
        item.put("createdAt", g.getCreatedAt());

        if (g.getOwnerUserId() != null) {
            item.put("ownerUserId", g.getOwnerUserId());
            if (ownerUsers != null) {
                User ou = ownerUsers.get(g.getOwnerUserId());
                if (ou != null) {
                    item.put("ownerName", ou.getUsername());
                    item.put("ownerAvatar", ou.getProfilePhoto());
                }
            }
        }

        PlatGroupMember m = memberMap.get(g.getId());
        if (m != null) {
            boolean isApproved = "approved".equals(m.getStatus());
            boolean isPending = "pending".equals(m.getStatus());
            boolean isManager = isApproved && ("owner".equals(m.getRole()) || "admin".equals(m.getRole()));
            boolean isOwnerRole = isApproved && "owner".equals(m.getRole());
            item.put("isMember", isApproved);
            item.put("managed", isManager);
            item.put("isOwner", isOwnerRole);
            item.put("myRole", m.getRole());
            item.put("hasPendingRequest", isPending);
            item.put("myStatus", isManager ? "managed" : isApproved ? "joined" : isPending ? "pending" : "none");
        } else {
            item.put("isMember", false);
            item.put("managed", false);
            item.put("isOwner", false);
            item.put("myRole", null);
            item.put("hasPendingRequest", false);
            item.put("myStatus", "none");
        }
        return item;
    }
}
