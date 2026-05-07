package com.lovecube.backend.growth.service;

import com.lovecube.backend.entity.Dynamic;
import com.lovecube.backend.growth.entity.GrowthEvent;
import com.lovecube.backend.growth.entity.UserGrowth;
import com.lovecube.backend.repository.DynamicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class GrowthBroadcastService {
    private static final String SCENE_TYPE_GROWTH = "GROWTH";

    private final DynamicRepository dynamicRepository;

    public GrowthBroadcastService(DynamicRepository dynamicRepository) {
        this.dynamicRepository = dynamicRepository;
    }

    @Transactional
    public void broadcastGroupCreated(GrowthEvent event) {
        String marker = marker("GROUP_CREATED", event.getId());
        publishIfAbsent(event.getActorUserId(), marker, "创建了新的团体，成长里程碑已达成");
    }

    @Transactional
    public void broadcastLevelUp(GrowthEvent event, UserGrowth userGrowth) {
        String marker = marker("LEVEL_UP", event.getId());
        publishIfAbsent(
                event.getActorUserId(),
                marker,
                "成长等级提升至 Lv." + userGrowth.getLevel() + "，继续保持！"
        );
    }

    private String marker(String type, Long eventId) {
        return "[GROWTH|" + type + "|event=" + eventId + "]";
    }

    private void publishIfAbsent(Long userId, String marker, String message) {
        if (dynamicRepository.existsByUserIdAndContentAndSceneTypeAndIsDeletedFalse(userId, marker, SCENE_TYPE_GROWTH)) {
            return;
        }
        Dynamic dynamic = new Dynamic();
        dynamic.setUserId(userId);
        dynamic.setContent(marker);
        dynamic.setImageUrls(null);
        dynamic.setLikeCount(0);
        dynamic.setCommentCount(0);
        dynamic.setShareCount(0);
        dynamic.setIsDeleted(false);
        dynamic.setSceneType(SCENE_TYPE_GROWTH);
        dynamic.setCreatedAt(LocalDateTime.now());
        dynamic.setUpdatedAt(LocalDateTime.now());
        dynamicRepository.save(dynamic);
    }
}
