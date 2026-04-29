package com.lovecube.backend.services;

import com.lovecube.backend.entity.OperationPost;
import com.lovecube.backend.repository.OperationPostRepository;
import com.lovecube.backend.repository.PlatGroupRepository;
import com.lovecube.backend.repository.PositiveShareRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PortalService {

    private final OperationPostRepository operationPostRepository;
    private final UserRepository userRepository;
    private final PlatGroupRepository platGroupRepository;
    private final PositiveShareRepository positiveShareRepository;
    private final HomeConfigService homeConfigService;

    public PortalService(
            OperationPostRepository operationPostRepository,
            UserRepository userRepository,
            PlatGroupRepository platGroupRepository,
            PositiveShareRepository positiveShareRepository,
            HomeConfigService homeConfigService
    ) {
        this.operationPostRepository = operationPostRepository;
        this.userRepository = userRepository;
        this.platGroupRepository = platGroupRepository;
        this.positiveShareRepository = positiveShareRepository;
        this.homeConfigService = homeConfigService;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> buildPortal() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("noticeBar", safeGet(() -> {
            List<OperationPost> notices = operationPostRepository.findByType(
                    "published", "notice", PageRequest.of(0, 1));
            return notices.isEmpty() ? Map.of() : toSummary(notices.get(0));
        }, Map.of()));

        result.put("stats", safeGet(this::buildStats, Map.of()));

        result.put("modules", safeGet(() -> {
            Map<String, Object> config = homeConfigService.getPublicHomeConfig();
            Object modules = config.get("modules");
            return modules instanceof List ? modules : List.of();
        }, List.of()));

        result.put("officialNews", safeGet(() ->
                operationPostRepository.findByType("published", "news", PageRequest.of(0, 5)),
                List.of()));

        result.put("changelogs", safeGet(() ->
                operationPostRepository.findByType("published", "changelog", PageRequest.of(0, 5)),
                List.of()));

        result.put("featuredContents", safeGet(() ->
                operationPostRepository.findByType("published", "featured", PageRequest.of(0, 4)),
                List.of()));

        result.put("activities", safeGet(() ->
                operationPostRepository.findByType("published", "activity", PageRequest.of(0, 3)),
                List.of()));

        return result;
    }

    private Map<String, Object> buildStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("userCount", safeGet(userRepository::count, 0L));
        stats.put("groupCount", safeGet(platGroupRepository::count, 0L));
        stats.put("shareCount", safeGet(
                () -> positiveShareRepository.countByStatusAndPublicVisible("PUBLISHED", true), 0L));
        return stats;
    }

    private Map<String, Object> toSummary(OperationPost post) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", post.getId());
        map.put("title", post.getTitle());
        map.put("summary", post.getSummary());
        map.put("publishTime", post.getPublishTime());
        return map;
    }

    private <T> T safeGet(SupplierWithException<T> supplier, T fallback) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return fallback;
        }
    }

    @FunctionalInterface
    interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
