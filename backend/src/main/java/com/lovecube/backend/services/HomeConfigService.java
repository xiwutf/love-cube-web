package com.lovecube.backend.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovecube.backend.entity.HomeConfig;
import com.lovecube.backend.repository.HomeConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class HomeConfigService {
    private static final String GROUP_HERO = "hero";
    private static final String GROUP_MODULE = "module";
    private static final String GROUP_ABILITY = "ability";
    private static final String GROUP_FOUNDATION = "foundation";
    private static final String GROUP_CHANGELOG = "changelog";
    private static final List<String> ALL_GROUPS = List.of(GROUP_HERO, GROUP_MODULE, GROUP_ABILITY, GROUP_FOUNDATION, GROUP_CHANGELOG);

    private final HomeConfigRepository homeConfigRepository;
    private final ObjectMapper objectMapper;

    public HomeConfigService(HomeConfigRepository homeConfigRepository, ObjectMapper objectMapper) {
        this.homeConfigRepository = homeConfigRepository;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getPublicHomeConfig() {
        return filterPublicResponse(buildResponse(homeConfigRepository.findByConfigGroupInOrderByConfigGroupAscSortOrderAscIdAsc(ALL_GROUPS)));
    }

    public Map<String, Object> getAdminHomeConfig() {
        return buildResponse(homeConfigRepository.findByConfigGroupInOrderByConfigGroupAscSortOrderAscIdAsc(ALL_GROUPS));
    }

    @Transactional
    public Map<String, Object> saveAdminHomeConfig(Map<String, Object> payload) {
        Map<String, Object> hero = toMap(payload.get("hero"));
        List<Map<String, Object>> modules = toListOfMap(payload.get("modules"));
        List<Map<String, Object>> abilities = toListOfMap(payload.get("abilities"));
        Map<String, Object> foundation = toMap(payload.get("foundation"));
        List<Map<String, Object>> changelog = toListOfMap(payload.get("changelog"));

        upsertSingle(GROUP_HERO, "hero-main", hero, 0, true);
        upsertList(GROUP_MODULE, modules, "moduleKey");
        upsertList(GROUP_ABILITY, abilities, "abilityKey");
        upsertSingle(GROUP_FOUNDATION, "foundation-main", foundation, 0, true);
        upsertList(GROUP_CHANGELOG, changelog, "version");

        return getAdminHomeConfig();
    }

    private Map<String, Object> buildResponse(List<HomeConfig> rows) {
        Map<String, Object> hero = defaultHero();
        Map<String, Object> foundation = defaultFoundation();
        List<Map<String, Object>> modules = defaultModules();
        List<Map<String, Object>> abilities = defaultAbilities();
        List<Map<String, Object>> changelog = defaultChangelog();

        Map<String, Map<String, Object>> moduleMap = new LinkedHashMap<>();
        for (Map<String, Object> item : modules) {
            moduleMap.put(String.valueOf(item.get("moduleKey")), new LinkedHashMap<>(item));
        }
        Map<String, Map<String, Object>> abilityMap = new LinkedHashMap<>();
        for (Map<String, Object> item : abilities) {
            abilityMap.put(String.valueOf(item.get("abilityKey")), new LinkedHashMap<>(item));
        }
        Map<String, Map<String, Object>> changelogMap = new LinkedHashMap<>();
        for (Map<String, Object> item : changelog) {
            changelogMap.put(String.valueOf(item.get("version")), new LinkedHashMap<>(item));
        }

        for (HomeConfig row : rows) {
            Map<String, Object> value = parseJson(row.getConfigValue());
            if (GROUP_HERO.equals(row.getConfigGroup())) {
                hero.putAll(value);
            } else if (GROUP_FOUNDATION.equals(row.getConfigGroup())) {
                foundation.putAll(value);
            } else if (GROUP_MODULE.equals(row.getConfigGroup())) {
                String key = row.getConfigKey();
                Map<String, Object> target = moduleMap.getOrDefault(key, new LinkedHashMap<>());
                target.putAll(value);
                target.put("moduleKey", key);
                target.put("sortOrder", row.getSortOrder());
                target.put("enabled", row.getEnabled());
                moduleMap.put(key, target);
            } else if (GROUP_ABILITY.equals(row.getConfigGroup())) {
                String key = row.getConfigKey();
                Map<String, Object> target = abilityMap.getOrDefault(key, new LinkedHashMap<>());
                target.putAll(value);
                target.put("abilityKey", key);
                target.put("sortOrder", row.getSortOrder());
                target.put("enabled", row.getEnabled());
                abilityMap.put(key, target);
            } else if (GROUP_CHANGELOG.equals(row.getConfigGroup())) {
                String key = row.getConfigKey();
                Map<String, Object> target = changelogMap.getOrDefault(key, new LinkedHashMap<>());
                target.putAll(value);
                target.put("version", key);
                target.put("sortOrder", row.getSortOrder());
                target.put("enabled", row.getEnabled());
                changelogMap.put(key, target);
            }
        }

        modules = moduleMap.values().stream()
                .sorted(Comparator.comparingInt(item -> toInt(item.get("sortOrder"), 0)))
                .toList();
        abilities = abilityMap.values().stream()
                .sorted(Comparator.comparingInt(item -> toInt(item.get("sortOrder"), 0)))
                .toList();
        changelog = changelogMap.values().stream()
                .sorted(Comparator.comparingInt(item -> toInt(item.get("sortOrder"), 0)))
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("hero", hero);
        result.put("modules", modules);
        result.put("abilities", abilities);
        result.put("foundation", foundation);
        result.put("changelog", changelog);
        result.put("banners", List.of());
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> filterPublicResponse(Map<String, Object> response) {
        Map<String, Object> result = new LinkedHashMap<>(response);
        result.put("modules", filterEnabledList((List<Map<String, Object>>) result.get("modules")));
        result.put("abilities", filterEnabledList((List<Map<String, Object>>) result.get("abilities")));
        result.put("changelog", filterEnabledList((List<Map<String, Object>>) result.get("changelog")));
        return result;
    }

    private List<Map<String, Object>> filterEnabledList(List<Map<String, Object>> list) {
        if (list == null) return List.of();
        return list.stream()
                .filter(item -> toBoolean(item.getOrDefault("enabled", true)))
                .toList();
    }

    private void upsertList(String group, List<Map<String, Object>> list, String keyField) {
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> item = new LinkedHashMap<>(list.get(i));
            String key = String.valueOf(item.getOrDefault(keyField, "")).trim();
            if (key.isEmpty()) key = group + "-" + i;
            int sort = toInt(item.getOrDefault("sortOrder", i), i);
            boolean enabled = toBoolean(item.getOrDefault("enabled", true));
            item.remove("sortOrder");
            item.remove("enabled");
            upsertSingle(group, key, item, sort, enabled);
        }
    }

    private void upsertSingle(String group, String key, Map<String, Object> value, int sort, boolean enabled) {
        HomeConfig item = homeConfigRepository.findByConfigGroupAndConfigKey(group, key).orElseGet(HomeConfig::new);
        item.setConfigGroup(group);
        item.setConfigKey(key);
        item.setSortOrder(sort);
        item.setEnabled(enabled);
        item.setConfigValue(writeJson(value));
        homeConfigRepository.save(item);
    }

    private Map<String, Object> defaultHero() {
        Map<String, Object> hero = new LinkedHashMap<>();
        hero.put("title", "Love Cube 多功能连接平台");
        hero.put("subtitle", "聚合内容资讯、活动服务、社交连接、本地服务与 AI 工具能力，为不同场景提供统一入口。");
        hero.put("primaryText", "进入模块中心");
        hero.put("primaryLink", "/modules");
        hero.put("secondaryText", "查看平台动态");
        hero.put("secondaryLink", "/articles");
        hero.put("imageUrl", "");
        return hero;
    }

    private List<Map<String, Object>> defaultModules() {
        return List.of(
                module("fellowship", "联谊交友", "资料认证、互动私信与安全治理", "/fellowship", "active", 1),
                module("events", "活动中心", "平台活动、主题活动与线下信息展示", "/events", "active", 2),
                module("articles", "内容资讯", "精选文章、平台资讯与内容沉淀", "/articles", "active", 3),
                module("announcements", "公告通知", "平台公告、规则说明和重要通知", "/announcements", "active", 4),
                module("local-services", "本地服务", "后续扩展招聘、二手车、生活服务等", "", "planned", 5),
                module("ai-tools", "AI 工具", "后续接入智能助手、效率工具和内容生成", "", "planned", 6)
        );
    }

    private List<Map<String, Object>> defaultAbilities() {
        return List.of(
                ability("fellowship", "联谊交友", "支持资料认证、互动私信、治理风控等完整链路。", 1),
                ability("events", "活动中心", "支持活动展示、活动主题页和信息发布能力。", 2),
                ability("articles", "内容资讯", "支持内容聚合、文章沉淀与资讯动态输出。", 3),
                ability("announcements", "公告通知", "支持规则说明、平台公告和重要消息触达。", 4)
        );
    }

    private Map<String, Object> defaultFoundation() {
        Map<String, Object> foundation = new LinkedHashMap<>();
        foundation.put("title", "平台基础能力");
        foundation.put("subtitle", "统一建设账号、运营、消息和治理，支撑多模块长期协同增长。");
        foundation.put("imageUrl", "");
        return foundation;
    }

    private List<Map<String, Object>> defaultChangelog() {
        return List.of(
                changelog("v1.4.0", "优化移动端体验，修复已知问题", "2026-04-29", 1),
                changelog("v1.3.0", "新增问卷调查功能，支持匿名填写", "2026-04-26", 2),
                changelog("v1.2.0", "上线每日心声板块，记录生活点滴", "2026-04-20", 3),
                changelog("v1.1.0", "优化登录流程，提升系统稳定性", "2026-04-15", 4),
                changelog("v1.0.0", "平台正式上线，基础功能发布", "2026-04-01", 5)
        );
    }

    private Map<String, Object> module(
            String key,
            String name,
            String description,
            String entryRoute,
            String status,
            int sortOrder
    ) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("moduleKey", key);
        item.put("title", name);
        item.put("desc", description);
        item.put("to", entryRoute);
        item.put("status", status);
        item.put("icon", switch (key) {
            case "fellowship" -> "联";
            case "events" -> "活";
            case "articles" -> "文";
            case "announcements" -> "告";
            case "ai-tools" -> "AI";
            default -> "服";
        });
        item.put("tone", switch (key) {
            case "fellowship" -> "tone-blue";
            case "events" -> "tone-cyan";
            case "articles" -> "tone-green";
            case "announcements" -> "tone-amber";
            case "ai-tools" -> "tone-rose";
            default -> "tone-violet";
        });
        item.put("coverUrl", "");
        item.put("enabled", true);
        item.put("sortOrder", sortOrder);
        return item;
    }

    private Map<String, Object> ability(String key, String title, String desc, int sortOrder) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("abilityKey", key);
        item.put("title", title);
        item.put("desc", desc);
        item.put("icon", switch (key) {
            case "fellowship" -> "联";
            case "events" -> "活";
            case "articles" -> "文";
            case "announcements" -> "告";
            default -> "能";
        });
        item.put("imageUrl", "");
        item.put("enabled", true);
        item.put("sortOrder", sortOrder);
        return item;
    }

    private Map<String, Object> changelog(String version, String title, String date, int sortOrder) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("version", version);
        item.put("title", title);
        item.put("date", date);
        item.put("enabled", true);
        item.put("sortOrder", sortOrder);
        return item;
    }

    private String writeJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map == null ? Map.of() : map);
        } catch (Exception ignored) {
            return "{}";
        }
    }

    private Map<String, Object> parseJson(String value) {
        if (value == null || value.isBlank()) return new LinkedHashMap<>();
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toListOfMap(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().filter(Map.class::isInstance).map(item -> (Map<String, Object>) item).toList();
        }
        return List.of();
    }

    private int toInt(Object value, int fallback) {
        if (value == null) return fallback;
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) return bool;
        return !"false".equalsIgnoreCase(String.valueOf(value));
    }
}
