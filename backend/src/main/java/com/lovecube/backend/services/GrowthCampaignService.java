package com.lovecube.backend.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovecube.backend.entity.FellowshipProfile;
import com.lovecube.backend.entity.FellowshipProfileMain;
import com.lovecube.backend.entity.GrowthCampaign;
import com.lovecube.backend.entity.GrowthCampaignClickEvent;
import com.lovecube.backend.entity.GrowthCampaignDelivery;
import com.lovecube.backend.entity.UserNotification;
import com.lovecube.backend.entity.UserProfile;
import com.lovecube.backend.growth.GrowthCampaignSegment;
import com.lovecube.backend.growth.GrowthCampaignTemplate;
import com.lovecube.backend.models.User;
import com.lovecube.backend.notification.NotificationCatalog;
import com.lovecube.backend.repository.FellowshipProfileMainRepository;
import com.lovecube.backend.repository.FellowshipProfileRepository;
import com.lovecube.backend.repository.GrowthCampaignClickEventRepository;
import com.lovecube.backend.repository.GrowthCampaignDeliveryRepository;
import com.lovecube.backend.repository.GrowthCampaignRepository;
import com.lovecube.backend.repository.UserNotificationRepository;
import com.lovecube.backend.repository.UserPhotoRepository;
import com.lovecube.backend.repository.UserProfileRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminFellowshipUserInsightService.InsightBatchContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GrowthCampaignService {
    private static final int SAMPLE_USER_LIMIT = 5;
    private static final int DEFAULT_DELIVERY_PAGE_SIZE = 20;

    private final UserRepository userRepository;
    private final UserPhotoRepository userPhotoRepository;
    private final FellowshipProfileMainRepository fellowshipProfileMainRepository;
    private final FellowshipProfileRepository fellowshipProfileRepository;
    private final UserProfileRepository userProfileRepository;
    private final VerificationService verificationService;
    private final AdminAuthService adminAuthService;
    private final AdminFellowshipUserInsightService adminFellowshipUserInsightService;
    private final GrowthCampaignRepository growthCampaignRepository;
    private final GrowthCampaignDeliveryRepository growthCampaignDeliveryRepository;
    private final GrowthCampaignClickEventRepository growthCampaignClickEventRepository;
    private final NotificationService notificationService;
    private final UserNotificationRepository userNotificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GrowthCampaignService(
            UserRepository userRepository,
            UserPhotoRepository userPhotoRepository,
            FellowshipProfileMainRepository fellowshipProfileMainRepository,
            FellowshipProfileRepository fellowshipProfileRepository,
            UserProfileRepository userProfileRepository,
            VerificationService verificationService,
            AdminAuthService adminAuthService,
            AdminFellowshipUserInsightService adminFellowshipUserInsightService,
            GrowthCampaignRepository growthCampaignRepository,
            GrowthCampaignDeliveryRepository growthCampaignDeliveryRepository,
            GrowthCampaignClickEventRepository growthCampaignClickEventRepository,
            NotificationService notificationService,
            UserNotificationRepository userNotificationRepository
    ) {
        this.userRepository = userRepository;
        this.userPhotoRepository = userPhotoRepository;
        this.fellowshipProfileMainRepository = fellowshipProfileMainRepository;
        this.fellowshipProfileRepository = fellowshipProfileRepository;
        this.userProfileRepository = userProfileRepository;
        this.verificationService = verificationService;
        this.adminAuthService = adminAuthService;
        this.adminFellowshipUserInsightService = adminFellowshipUserInsightService;
        this.growthCampaignRepository = growthCampaignRepository;
        this.growthCampaignDeliveryRepository = growthCampaignDeliveryRepository;
        this.growthCampaignClickEventRepository = growthCampaignClickEventRepository;
        this.notificationService = notificationService;
        this.userNotificationRepository = userNotificationRepository;
    }

    public Map<String, Object> previewSegment(User operator, String segmentCode) {
        GrowthCampaignSegment segment = parseSegment(segmentCode);
        List<SegmentUserRow> rows = resolveSegmentUsers(operator, segment.name());
        List<Map<String, Object>> sampleUsers = rows.stream()
                .limit(SAMPLE_USER_LIMIT)
                .map(this::toSampleUserView)
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("segment", segment.name());
        result.put("targetCount", rows.size());
        result.put("sampleUsers", sampleUsers);
        result.put("suggestedTemplateCode", segment.suggestedTemplateCode());
        GrowthCampaignTemplate.fromCode(segment.suggestedTemplateCode())
                .ifPresent(template -> result.put("suggestedTemplate", toTemplateView(template)));
        return result;
    }

    @Transactional
    public Map<String, Object> createAndSend(
            User operator,
            String segmentCode,
            String templateCode,
            String channel,
            String name
    ) {
        GrowthCampaignSegment segment = parseSegment(segmentCode);
        GrowthCampaignTemplate template = parseTemplate(templateCode);
        String resolvedChannel = channel == null || channel.isBlank() ? "IN_APP" : channel.trim().toUpperCase(Locale.ROOT);
        String campaignName = name == null || name.isBlank()
                ? segment.name() + " 运营触达"
                : name.trim();

        List<SegmentUserRow> targets = resolveSegmentUsers(operator, segment.name());
        if (targets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前分群无目标用户");
        }

        GrowthCampaign campaign = new GrowthCampaign();
        campaign.setName(campaignName);
        campaign.setSegment(segment.name());
        campaign.setTemplateCode(template.getCode());
        campaign.setChannel(resolvedChannel);
        campaign.setTargetCount(targets.size());
        campaign.setSentCount(0);
        campaign.setOpenedCount(0);
        campaign.setClickedCount(0);
        campaign.setCompletedCount(0);
        campaign.setStatus("SENT");
        campaign.setCreatedBy(operator.getUserid());
        campaign = growthCampaignRepository.save(campaign);

        int sentCount = 0;
        int failedCount = 0;
        for (SegmentUserRow row : targets) {
            GrowthCampaignDelivery delivery = new GrowthCampaignDelivery();
            delivery.setCampaignId(campaign.getId());
            delivery.setUserId(row.user().getUserid());
            delivery.setChannel(resolvedChannel);
            delivery.setTemplateCode(template.getCode());
            delivery.setActionUrl(template.getActionUrl());
            delivery.setStatus("PENDING");
            delivery.setCompletionSnapshotBefore(serializeSnapshot(buildCompletionSnapshot(row)));
            delivery = growthCampaignDeliveryRepository.save(delivery);

            try {
                UserNotification notification = notificationService.createNotification(
                        row.user().getUserid(),
                        NotificationCatalog.TYPE_GROWTH_OPERATOR_REMINDER,
                        template.getTitle(),
                        template.getContent(),
                        template.getNotificationLinkUrl(),
                        NotificationCatalog.RELATED_GROWTH_CAMPAIGN_DELIVERY,
                        String.valueOf(delivery.getId())
                );
                if (notification == null) {
                    delivery.setStatus("FAILED");
                    delivery.setErrorMessage("用户已关闭站内通知");
                    failedCount += 1;
                } else {
                    delivery.setStatus("SENT");
                    delivery.setSentAt(LocalDateTime.now());
                    delivery.setNotificationId(notification.getId());
                    sentCount += 1;
                }
            } catch (Exception ex) {
                delivery.setStatus("FAILED");
                delivery.setErrorMessage(truncateError(ex.getMessage()));
                failedCount += 1;
            }
            growthCampaignDeliveryRepository.save(delivery);
        }

        campaign.setSentCount(sentCount);
        campaign.setStatus(failedCount == targets.size() ? "FAILED" : "SENT");
        growthCampaignRepository.save(campaign);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("campaignId", campaign.getId());
        result.put("targetCount", targets.size());
        result.put("sentCount", sentCount);
        result.put("failedCount", failedCount);
        return result;
    }

    @Transactional
    public Map<String, Object> recordDeliveryClick(
            User user,
            Long deliveryId,
            String userAgent,
            String ipAddress
    ) {
        if (user == null || user.getUserid() == null || deliveryId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "参数无效");
        }
        GrowthCampaignDelivery delivery = growthCampaignDeliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "触达记录不存在"));
        if (!user.getUserid().equals(delivery.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权上报该触达点击");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean firstClick = delivery.getClickedAt() == null;
        int currentCount = delivery.getClickedCount() == null ? 0 : delivery.getClickedCount();
        delivery.setClickedCount(currentCount + 1);
        delivery.setLastClickedAt(now);
        if (firstClick) {
            delivery.setClickedAt(now);
        }
        growthCampaignDeliveryRepository.save(delivery);

        GrowthCampaign campaign = growthCampaignRepository.findById(delivery.getCampaignId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        if (firstClick) {
            int campaignClicked = campaign.getClickedCount() == null ? 0 : campaign.getClickedCount();
            campaign.setClickedCount(campaignClicked + 1);
            growthCampaignRepository.save(campaign);
        }

        String actionUrl = delivery.getActionUrl();
        if (actionUrl == null || actionUrl.isBlank()) {
            actionUrl = GrowthCampaignTemplate.fromCode(delivery.getTemplateCode())
                    .map(GrowthCampaignTemplate::getActionUrl)
                    .orElse(null);
        }

        GrowthCampaignClickEvent event = new GrowthCampaignClickEvent();
        event.setCampaignId(delivery.getCampaignId());
        event.setDeliveryId(delivery.getId());
        event.setUserId(user.getUserid());
        event.setTemplateCode(delivery.getTemplateCode());
        event.setActionUrl(actionUrl);
        event.setClickedAt(now);
        event.setUserAgent(truncate(userAgent, 500));
        event.setIpAddress(truncate(ipAddress, 64));
        growthCampaignClickEventRepository.save(event);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("actionUrl", actionUrl);
        result.put("deliveryId", delivery.getId());
        result.put("firstClick", firstClick);
        return result;
    }

    public void enrichGrowthNotification(UserNotification notification, Map<String, Object> row) {
        if (notification == null || row == null) {
            return;
        }
        if (!NotificationCatalog.TYPE_GROWTH_OPERATOR_REMINDER.equals(notification.getType())) {
            return;
        }

        Long deliveryId = null;
        String actionUrl = notification.getLinkUrl();

        if (NotificationCatalog.RELATED_GROWTH_CAMPAIGN_DELIVERY.equals(notification.getRelatedType())
                && notification.getRelatedId() != null && !notification.getRelatedId().isBlank()) {
            try {
                deliveryId = Long.parseLong(notification.getRelatedId().trim());
            } catch (NumberFormatException ignored) {
                /* fall through */
            }
        }
        if (deliveryId == null && notification.getId() != null) {
            deliveryId = growthCampaignDeliveryRepository.findByNotificationId(notification.getId())
                    .map(GrowthCampaignDelivery::getId)
                    .orElse(null);
        }
        if (deliveryId != null) {
            row.put("deliveryId", deliveryId);
            GrowthCampaignDelivery delivery = growthCampaignDeliveryRepository.findById(deliveryId).orElse(null);
            if (delivery != null && delivery.getActionUrl() != null && !delivery.getActionUrl().isBlank()) {
                actionUrl = delivery.getActionUrl();
            }
        }
        if (actionUrl != null && !actionUrl.isBlank()) {
            row.put("actionUrl", actionUrl);
        }
    }

    public List<Map<String, Object>> listCampaigns() {
        return growthCampaignRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toCampaignListView)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getCampaignDetail(Long campaignId, Integer page, Integer size) {
        GrowthCampaign campaign = growthCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        int pageIndex = page == null || page < 1 ? 0 : page - 1;
        int pageSize = size == null || size < 1 ? DEFAULT_DELIVERY_PAGE_SIZE : Math.min(size, 100);
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<GrowthCampaignDelivery> deliveryPage =
                growthCampaignDeliveryRepository.findByCampaignIdOrderByIdAsc(campaignId, pageable);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("campaign", toCampaignListView(campaign));
        GrowthCampaignTemplate.fromCode(campaign.getTemplateCode())
                .ifPresent(template -> result.put("template", toTemplateView(template)));
        result.put("deliveries", deliveryPage.getContent().stream()
                .map(this::toDeliveryView)
                .collect(Collectors.toList()));
        result.put("deliveryPage", pageIndex + 1);
        result.put("deliveryPageSize", pageSize);
        result.put("deliveryTotal", deliveryPage.getTotalElements());
        result.put("deliveryTotalPages", deliveryPage.getTotalPages());
        return result;
    }

    @Transactional
    public Map<String, Object> refreshConversion(Long campaignId) {
        GrowthCampaign campaign = growthCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动不存在"));
        GrowthCampaignTemplate template = parseTemplate(campaign.getTemplateCode());
        List<GrowthCampaignDelivery> deliveries = growthCampaignDeliveryRepository.findByCampaignId(campaignId);

        InsightBatchContext ctx = loadInsightContextForOperator(null);
        Map<Long, User> userById = userRepository.findAll().stream()
                .filter(u -> u.getUserid() != null)
                .collect(Collectors.toMap(User::getUserid, u -> u, (a, b) -> a));

        for (GrowthCampaignDelivery delivery : deliveries) {
            if (!"SENT".equals(delivery.getStatus()) && !"COMPLETED".equals(delivery.getStatus())) {
                continue;
            }
            User user = userById.get(delivery.getUserId());
            if (user == null) {
                continue;
            }
            SegmentUserRow row = buildSegmentUserRow(user, ctx);
            Map<String, Object> afterSnapshot = buildCompletionSnapshot(row);
            delivery.setCompletionSnapshotAfter(serializeSnapshot(afterSnapshot));

            if (delivery.getNotificationId() != null) {
                userNotificationRepository.findById(delivery.getNotificationId()).ifPresent(notification -> {
                    if (Boolean.TRUE.equals(notification.getIsRead()) && delivery.getOpenedAt() == null) {
                        delivery.setOpenedAt(notification.getReadAt() != null
                                ? notification.getReadAt()
                                : LocalDateTime.now());
                    }
                });
            }

            Map<String, Object> beforeSnapshot = deserializeSnapshot(delivery.getCompletionSnapshotBefore());
            boolean goalMet = template.isGoalMet(beforeSnapshot, afterSnapshot);
            if (goalMet) {
                if (delivery.getCompletedAt() == null) {
                    delivery.setCompletedAt(LocalDateTime.now());
                }
                delivery.setStatus("COMPLETED");
            }
            growthCampaignDeliveryRepository.save(delivery);
        }

        int openedCount = (int) deliveries.stream().filter(d -> d.getOpenedAt() != null).count();
        int completedCount = (int) deliveries.stream().filter(d -> d.getCompletedAt() != null).count();
        campaign.setOpenedCount(openedCount);
        campaign.setCompletedCount(completedCount);
        growthCampaignRepository.save(campaign);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("campaignId", campaignId);
        result.put("openedCount", openedCount);
        result.put("completedCount", completedCount);
        result.put("clickedCount", campaign.getClickedCount());
        return result;
    }

    private List<SegmentUserRow> resolveSegmentUsers(User operator, String segment) {
        boolean hiddenSuperAdminOperator = adminAuthService.isHiddenSuperAdmin(operator);
        List<User> visibleUsers = userRepository.findAll().stream()
                .filter(user -> hiddenSuperAdminOperator || !adminAuthService.isHiddenSuperAdmin(user))
                .collect(Collectors.toList());
        InsightBatchContext ctx = loadInsightContext(visibleUsers);
        Map<Long, FellowshipProfileMain> mainByUser = ctx.mainByUser();
        Map<Long, UserProfile> userProfileByUser = ctx.userProfileByUser();

        List<SegmentUserRow> rows = new ArrayList<>();
        for (User user : visibleUsers) {
            Map<String, Object> insight = adminFellowshipUserInsightService.buildInsightFields(user, ctx);
            LocalDateTime lastLoginAt = resolveLastLoginAt(
                    user,
                    mainByUser.get(user.getUserid()),
                    userProfileByUser.get(user.getUserid())
            );
            if (adminFellowshipUserInsightService.matchesSegment(user, insight, lastLoginAt, segment)) {
                rows.add(new SegmentUserRow(user, insight, lastLoginAt, ctx));
            }
        }
        return rows;
    }

    private SegmentUserRow buildSegmentUserRow(User user, InsightBatchContext ctx) {
        Map<String, Object> insight = adminFellowshipUserInsightService.buildInsightFields(user, ctx);
        LocalDateTime lastLoginAt = resolveLastLoginAt(
                user,
                ctx.mainByUser().get(user.getUserid()),
                ctx.userProfileByUser().get(user.getUserid())
        );
        return new SegmentUserRow(user, insight, lastLoginAt, ctx);
    }

    private InsightBatchContext loadInsightContextForOperator(User operator) {
        boolean hiddenSuperAdminOperator = operator != null && adminAuthService.isHiddenSuperAdmin(operator);
        List<User> visibleUsers = userRepository.findAll().stream()
                .filter(user -> hiddenSuperAdminOperator || !adminAuthService.isHiddenSuperAdmin(user))
                .collect(Collectors.toList());
        return loadInsightContext(visibleUsers);
    }

    private InsightBatchContext loadInsightContext(List<User> visibleUsers) {
        List<Long> userIds = visibleUsers.stream()
                .map(User::getUserid)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        Map<Long, Long> uploadedPhotoCountMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (Object[] row : userPhotoRepository.countGroupedByUserIds(userIds)) {
                if (row == null || row.length < 2) {
                    continue;
                }
                Long uid = row[0] instanceof Number ? ((Number) row[0]).longValue() : null;
                Long count = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
                if (uid != null) {
                    uploadedPhotoCountMap.put(uid, count);
                }
            }
        }
        Map<Long, FellowshipProfileMain> mainProfileMap = userIds.isEmpty()
                ? Map.of()
                : fellowshipProfileMainRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(FellowshipProfileMain::getUserId, item -> item, (a, b) -> a));
        Map<Long, FellowshipProfile> legacyProfileMap = userIds.isEmpty()
                ? Map.of()
                : fellowshipProfileRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(FellowshipProfile::getUserId, item -> item, (a, b) -> a));
        Map<Long, UserProfile> userProfileMap = userIds.isEmpty()
                ? Map.of()
                : userProfileRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, item -> item, (a, b) -> a));
        Map<Long, Map<String, Boolean>> verifySummaryMap = verificationService.getBatchSummary(userIds);
        return adminFellowshipUserInsightService.prepareContext(
                visibleUsers,
                uploadedPhotoCountMap,
                verifySummaryMap,
                mainProfileMap,
                legacyProfileMap,
                userProfileMap
        );
    }

    private Map<String, Object> buildCompletionSnapshot(SegmentUserRow row) {
        User user = row.user();
        Map<String, Object> insight = row.insight();
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("completionRate", insight.getOrDefault("profileCompletionRate", 0));
        snapshot.put("verificationTier", insight.getOrDefault("verificationTier", "none"));
        snapshot.put("fellowshipEnabled", Boolean.TRUE.equals(user.getFellowshipEnabled()));
        snapshot.put("photoCount", row.ctx().photoCountByUser().getOrDefault(user.getUserid(), 0L));
        FellowshipProfileMain main = row.ctx().mainByUser().get(user.getUserid());
        FellowshipProfile legacy = row.ctx().legacyByUser().get(user.getUserid());
        UserProfile userProfile = row.ctx().userProfileByUser().get(user.getUserid());
        snapshot.put("city", firstNonBlank(
                user.getLocation(),
                main == null ? null : main.getCity(),
                legacy == null ? null : legacy.getCity(),
                userProfile == null ? null : userProfile.getCity()
        ));
        return snapshot;
    }

    private Map<String, Object> toSampleUserView(SegmentUserRow row) {
        Map<String, Object> insight = row.insight();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> missing = (List<Map<String, Object>>) insight.getOrDefault(
                "profileMissingItems", List.of());
        List<String> missingItems = missing.stream()
                .map(m -> String.valueOf(m.getOrDefault("key", "")))
                .filter(k -> !k.isBlank())
                .collect(Collectors.toList());

        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", row.user().getUserid());
        view.put("nickname", row.user().getUsername() == null ? "" : row.user().getUsername());
        view.put("profileCompletionRate", insight.getOrDefault("profileCompletionRate", 0));
        view.put("verificationTier", String.valueOf(insight.getOrDefault("verificationTier", "none"))
                .toUpperCase(Locale.ROOT));
        view.put("missingItems", missingItems);
        return view;
    }

    private Map<String, Object> toCampaignListView(GrowthCampaign campaign) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", campaign.getId());
        view.put("name", campaign.getName());
        view.put("segment", campaign.getSegment());
        view.put("templateCode", campaign.getTemplateCode());
        view.put("channel", campaign.getChannel());
        view.put("targetCount", campaign.getTargetCount());
        view.put("sentCount", campaign.getSentCount());
        view.put("openedCount", campaign.getOpenedCount());
        view.put("clickedCount", campaign.getClickedCount());
        view.put("completedCount", campaign.getCompletedCount());
        view.put("status", campaign.getStatus());
        view.put("createdAt", campaign.getCreatedAt());
        view.put("updatedAt", campaign.getUpdatedAt());
        putFunnelRates(view, campaign);
        return view;
    }

    private Map<String, Object> toDeliveryView(GrowthCampaignDelivery delivery) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", delivery.getId());
        view.put("userId", delivery.getUserId());
        view.put("channel", delivery.getChannel());
        view.put("templateCode", delivery.getTemplateCode());
        view.put("actionUrl", delivery.getActionUrl());
        view.put("status", delivery.getStatus());
        view.put("sentAt", delivery.getSentAt());
        view.put("openedAt", delivery.getOpenedAt());
        view.put("clickedAt", delivery.getClickedAt());
        view.put("clickedCount", delivery.getClickedCount());
        view.put("lastClickedAt", delivery.getLastClickedAt());
        view.put("completedAt", delivery.getCompletedAt());
        view.put("errorMessage", delivery.getErrorMessage());
        view.put("completionSnapshotBefore", deserializeSnapshot(delivery.getCompletionSnapshotBefore()));
        view.put("completionSnapshotAfter", deserializeSnapshot(delivery.getCompletionSnapshotAfter()));
        return view;
    }

    private Map<String, Object> toTemplateView(GrowthCampaignTemplate template) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("code", template.getCode());
        view.put("title", template.getTitle());
        view.put("content", template.getContent());
        view.put("actionUrl", template.getActionUrl());
        return view;
    }

    private GrowthCampaignSegment parseSegment(String segmentCode) {
        return GrowthCampaignSegment.fromCode(segmentCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效分群 segment"));
    }

    private GrowthCampaignTemplate parseTemplate(String templateCode) {
        return GrowthCampaignTemplate.fromCode(templateCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效模板 templateCode"));
    }

    private String serializeSnapshot(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private Map<String, Object> deserializeSnapshot(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private static LocalDateTime resolveLastLoginAt(
            User user,
            FellowshipProfileMain mainProfile,
            UserProfile userProfile
    ) {
        if (userProfile != null && userProfile.getLastActiveTime() != null) {
            return userProfile.getLastActiveTime();
        }
        if (mainProfile != null && mainProfile.getLastActiveAt() != null) {
            return mainProfile.getLastActiveAt();
        }
        return user.getUpdatedAt();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private static String truncateError(String message) {
        if (message == null) {
            return "发送失败";
        }
        return message.length() > 480 ? message.substring(0, 480) : message;
    }

    private static String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLen ? value.substring(0, maxLen) : value;
    }

    private static void putFunnelRates(Map<String, Object> view, GrowthCampaign campaign) {
        int sent = safeInt(campaign.getSentCount());
        int opened = safeInt(campaign.getOpenedCount());
        int clicked = safeInt(campaign.getClickedCount());
        int completed = safeInt(campaign.getCompletedCount());
        view.put("openRate", ratio(opened, sent));
        view.put("clickRate", ratio(clicked, sent));
        view.put("completeRate", ratio(completed, sent));
        view.put("clickToCompleteRate", ratio(completed, clicked));
    }

    private static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private static Double ratio(int numerator, int denominator) {
        if (denominator <= 0) {
            return null;
        }
        return (double) numerator / denominator;
    }

    private record SegmentUserRow(
            User user,
            Map<String, Object> insight,
            LocalDateTime lastLoginAt,
            InsightBatchContext ctx
    ) {
    }
}
