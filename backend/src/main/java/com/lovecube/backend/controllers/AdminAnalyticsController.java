package com.lovecube.backend.controllers;

import com.lovecube.backend.repository.SiteVisitLogRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.PermissionConstants;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {
    private final SiteVisitLogRepository siteVisitLogRepository;
    private final AdminAuthService adminAuthService;

    public AdminAnalyticsController(SiteVisitLogRepository siteVisitLogRepository, AdminAuthService adminAuthService) {
        this.siteVisitLogRepository = siteVisitLogRepository;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping("/overview")
    public Map<String, Object> getOverview(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.SYSTEM_MANAGE);
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        long todayPv = siteVisitLogRepository.countByCreatedAtBetween(todayStart, now);
        long todayUv = siteVisitLogRepository.countDistinctVisitorIdBetween(todayStart, now);
        long yesterdayPv = siteVisitLogRepository.countByCreatedAtBetween(yesterdayStart, todayStart);
        long yesterdayUv = siteVisitLogRepository.countDistinctVisitorIdBetween(yesterdayStart, todayStart);
        long totalPv = siteVisitLogRepository.count();
        long totalUv = siteVisitLogRepository.countDistinctVisitors();
        long onlineUsers = siteVisitLogRepository.countDistinctVisitorIdSince(now.minusMinutes(5));

        return Map.of(
                "todayPv", todayPv,
                "todayUv", todayUv,
                "yesterdayPv", yesterdayPv,
                "yesterdayUv", yesterdayUv,
                "totalPv", totalPv,
                "totalUv", totalUv,
                "onlineUsers", onlineUsers
        );
    }

    @GetMapping("/trend")
    public Map<String, Object> getTrend(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "range", defaultValue = "7d") String range
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.SYSTEM_MANAGE);
        LocalDateTime start = resolveStart(range);
        LocalDateTime end = LocalDateTime.now();
        List<Object[]> rows = siteVisitLogRepository.aggregateTrend(start, end);
        Map<String, Object> pointMap = new HashMap<>();
        for (Object[] row : rows) {
            pointMap.put(String.valueOf(row[0]), Map.of(
                    "date", String.valueOf(row[0]),
                    "pv", toLong(row[1]),
                    "uv", toLong(row[2])
            ));
        }

        List<Map<String, Object>> points = new ArrayList<>();
        LocalDate cursor = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        while (!cursor.isAfter(endDate)) {
            String date = cursor.toString();
            Object point = pointMap.get(date);
            if (point instanceof Map<?, ?> p) {
                points.add(Map.of(
                        "date", String.valueOf(p.get("date")),
                        "pv", toLong(p.get("pv")),
                        "uv", toLong(p.get("uv"))
                ));
            } else {
                points.add(Map.of("date", date, "pv", 0, "uv", 0));
            }
            cursor = cursor.plusDays(1);
        }
        return Map.of("points", points);
    }

    @GetMapping("/top-pages")
    public Map<String, Object> getTopPages(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "range", defaultValue = "7d") String range
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.SYSTEM_MANAGE);
        LocalDateTime start = resolveStart(range);
        List<Map<String, Object>> items = siteVisitLogRepository.aggregateTopPages(start, LocalDateTime.now(), 20)
                .stream()
                .map(row -> Map.<String, Object>of(
                        "url", row[0] == null ? "/" : String.valueOf(row[0]),
                        "pv", toLong(row[1]),
                        "uv", toLong(row[2])
                ))
                .toList();
        return Map.of("items", items);
    }

    @GetMapping("/sources")
    public Map<String, Object> getSources(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "range", defaultValue = "7d") String range
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.SYSTEM_MANAGE);
        LocalDateTime start = resolveStart(range);
        List<String> referrers = siteVisitLogRepository.findReferrers(start, LocalDateTime.now());
        long direct = 0;
        long search = 0;
        long external = 0;

        for (String referrer : referrers) {
            if (referrer == null || referrer.isBlank()) {
                direct++;
                continue;
            }
            String text = referrer.toLowerCase();
            if (text.contains("baidu.") || text.contains("google.") || text.contains("bing.") || text.contains("sogou.") || text.contains("so.com")) {
                search++;
            } else if (text.contains("xifg.com.cn") || text.contains("localhost")) {
                direct++;
            } else {
                external++;
            }
        }
        return Map.of("items", List.of(
                Map.of("name", "直接访问", "count", direct),
                Map.of("name", "搜索引擎", "count", search),
                Map.of("name", "外部来源", "count", external)
        ));
    }

    @GetMapping("/client-distribution")
    public Map<String, Object> getClientDistribution(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "range", defaultValue = "7d") String range
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.SYSTEM_MANAGE);
        LocalDateTime start = resolveStart(range);
        LocalDateTime end = LocalDateTime.now();
        return Map.of(
                "devices", mapDistribution(siteVisitLogRepository.aggregateDevices(start, end)),
                "browsers", mapDistribution(siteVisitLogRepository.aggregateBrowsers(start, end)),
                "os", mapDistribution(siteVisitLogRepository.aggregateOs(start, end))
        );
    }

    @GetMapping("/visitors")
    public Map<String, Object> getVisitors(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.SYSTEM_MANAGE);
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 5), 100);
        int offset = (safePage - 1) * safePageSize;
        List<Map<String, Object>> visitors = siteVisitLogRepository.findLatestVisitors(offset, safePageSize)
                .stream()
                .map(row -> Map.<String, Object>of(
                        "id", toLong(row[0]),
                        "visitorId", row[1] == null ? "-" : String.valueOf(row[1]),
                        "path", row[2] == null ? "/" : String.valueOf(row[2]),
                        "ip", row[3] == null ? "-" : String.valueOf(row[3]),
                        "deviceType", row[4] == null ? "other" : String.valueOf(row[4]),
                        "browser", row[5] == null ? "other" : String.valueOf(row[5]),
                        "updatedAt", row[6] == null ? null : String.valueOf(row[6])
                ))
                .toList();
        return Map.of(
                "total", siteVisitLogRepository.countDistinctVisitors(),
                "visitors", visitors
        );
    }

    private List<Map<String, Object>> mapDistribution(List<Object[]> rows) {
        return rows.stream()
                .map(row -> Map.<String, Object>of(
                        "name", row[0] == null || String.valueOf(row[0]).isBlank() ? "other" : String.valueOf(row[0]),
                        "count", toLong(row[1])
                ))
                .toList();
    }

    private LocalDateTime resolveStart(String range) {
        String normalized = (range == null ? "7d" : range).trim().toLowerCase();
        return switch (normalized) {
            case "today" -> LocalDate.now().atStartOfDay();
            case "30d" -> LocalDate.now().minusDays(29).atStartOfDay();
            case "90d" -> LocalDate.now().minusDays(89).atStartOfDay();
            default -> LocalDate.now().minusDays(6).atStartOfDay();
        };
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignore) {
            return 0L;
        }
    }
}
