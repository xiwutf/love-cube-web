package com.lovecube.backend.services;

import com.lovecube.backend.entity.Dynamic;
import com.lovecube.backend.entity.InterestTopic;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.DynamicRepository;
import com.lovecube.backend.repository.InterestTopicRepository;
import com.lovecube.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 2026 端午社区氛围：破冰动态（6/16 起）、收官动态（6/21 起）、话题接龙下线（6/22 起）。
 */
@Service
public class DragonBoat2026CampaignService {

    private static final Logger log = LoggerFactory.getLogger(DragonBoat2026CampaignService.class);

    private static final LocalDate CAMPAIGN_WINDOW_START = LocalDate.of(2026, 6, 16);
    private static final LocalDate CLOSING_POST_DATE = LocalDate.of(2026, 6, 21);
    private static final LocalDate TOPIC_DISABLE_DATE = LocalDate.of(2026, 6, 22);
    private static final LocalDate CAMPAIGN_WINDOW_END = LocalDate.of(2026, 6, 25);

    private static final String TOPIC_TITLE = "家乡端午怎么过";
    private static final String MARKER_ICEBREAKER = "[CAMPAIGN|DRAGON_BOAT_2026|ICEBREAKER]";
    private static final String MARKER_CLOSING = "[CAMPAIGN|DRAGON_BOAT_2026|CLOSING]";
    private static final String SCENE_FELLOWSHIP = "FELLOWSHIP";

    private static final String ICEBREAKER_CONTENT = """
            端午假期来啦 🌿

            今天开启 #端午晒一晒：
            · 粽子、家常、祝福、独处时光都算
            · 不用精致，真实就好

            我先来：形状偶尔像石头，味道一直很像家。
            #端午晒一晒 #丑粽子大赛

            敢晒的都不是完美主义者，欢迎跟帖～""";

    private static final String CLOSING_CONTENT = """
            端午这几天，我们在广场看到了：
            · 丑得可爱但好吃的粽子
            · 一个人安静看书的下午
            · 给远方朋友的一句祝福
            谢谢每一位愿意开口的伙伴。
            下一个话题，我们继续慢慢聊 🌿
            #端午晒一晒""";

    private static final List<String> PUBLISHER_ROLES = List.of("ROOT", "SUPER_ADMIN", "ADMIN");

    private final DynamicRepository dynamicRepository;
    private final InterestTopicRepository interestTopicRepository;
    private final UserRepository userRepository;

    public DragonBoat2026CampaignService(
            DynamicRepository dynamicRepository,
            InterestTopicRepository interestTopicRepository,
            UserRepository userRepository
    ) {
        this.dynamicRepository = dynamicRepository;
        this.interestTopicRepository = interestTopicRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void runScheduledTasks() {
        LocalDate today = LocalDate.now();
        if (today.isBefore(CAMPAIGN_WINDOW_START) || today.isAfter(CAMPAIGN_WINDOW_END)) {
            return;
        }
        publishIcebreakerPostIfNeeded();
        if (!today.isBefore(CLOSING_POST_DATE)) {
            publishClosingPostIfNeeded();
        }
        if (!today.isBefore(TOPIC_DISABLE_DATE)) {
            disableHolidayTopicIfNeeded();
        }
    }

    private void publishIcebreakerPostIfNeeded() {
        publishCampaignDynamicIfNeeded(MARKER_ICEBREAKER, ICEBREAKER_CONTENT, "破冰");
    }

    private void publishClosingPostIfNeeded() {
        publishCampaignDynamicIfNeeded(MARKER_CLOSING, CLOSING_CONTENT, "收官");
    }

    private void publishCampaignDynamicIfNeeded(String marker, String content, String logLabel) {
        if (dynamicRepository.existsByMarkerAndSceneTypeAndIsDeletedFalse(marker, SCENE_FELLOWSHIP)) {
            return;
        }
        Optional<User> publisher = resolveCampaignPublisher();
        if (publisher.isEmpty()) {
            log.warn("端午{}动态未发布：未找到 ROOT/SUPER_ADMIN/ADMIN 账号", logLabel);
            return;
        }
        Dynamic dynamic = new Dynamic();
        dynamic.setUserId(publisher.get().getUserid());
        dynamic.setContent(content.trim());
        dynamic.setMarker(marker);
        dynamic.setImageUrls(null);
        dynamic.setLikeCount(0);
        dynamic.setCommentCount(0);
        dynamic.setShareCount(0);
        dynamic.setIsDeleted(false);
        dynamic.setSceneType(SCENE_FELLOWSHIP);
        dynamic.setCreatedAt(LocalDateTime.now());
        dynamic.setUpdatedAt(LocalDateTime.now());
        dynamicRepository.save(dynamic);
        log.info("端午{}动态已发布 userId={}", logLabel, publisher.get().getUserid());
    }

    private void disableHolidayTopicIfNeeded() {
        InterestTopic topic = interestTopicRepository.findFirstByTitle(TOPIC_TITLE).orElse(null);
        if (topic == null || !Integer.valueOf(1).equals(topic.getEnabled())) {
            return;
        }
        topic.setEnabled(0);
        interestTopicRepository.save(topic);
        log.info("端午话题已下线 title={}", TOPIC_TITLE);
    }

    private Optional<User> resolveCampaignPublisher() {
        List<User> candidates = userRepository.findByRoleInOrderByCreatedAtAsc(PUBLISHER_ROLES);
        return candidates.stream()
                .min(Comparator.comparingInt(this::publisherPriority).thenComparing(User::getUserid));
    }

    private int publisherPriority(User user) {
        String role = user.getRole() == null ? "" : user.getRole().toUpperCase(Locale.ROOT);
        return switch (role) {
            case "ROOT" -> 0;
            case "SUPER_ADMIN" -> 1;
            case "ADMIN" -> 2;
            default -> 9;
        };
    }
}
