package com.lovecube.backend.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovecube.backend.entity.Dynamic;
import com.lovecube.backend.entity.DynamicComment;
import com.lovecube.backend.entity.DynamicLike;
import com.lovecube.backend.models.User;
import com.lovecube.backend.notification.NotificationCatalog;
import com.lovecube.backend.repository.DynamicCommentRepository;
import com.lovecube.backend.repository.DynamicLikeRepository;
import com.lovecube.backend.repository.DynamicRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DynamicService {
    private static final String SCENE_TYPE_FELLOWSHIP = "FELLOWSHIP";

    @Autowired
    private DynamicRepository dynamicRepository;

    @Autowired
    private DynamicLikeRepository dynamicLikeRepository;

    @Autowired
    private DynamicCommentRepository dynamicCommentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> getDynamicList(int pageNum, int pageSize, Long currentUserId) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<Dynamic> dynamicPage = dynamicRepository.findByIsDeletedFalseAndSceneTypeOrderByCreatedAtDesc(pageable, SCENE_TYPE_FELLOWSHIP);

        List<Dynamic> dynamics = dynamicPage.getContent().stream()
            .map(dynamic -> enrichDynamicInfo(dynamic, currentUserId))
            .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("list", dynamics);
        result.put("total", dynamicPage.getTotalElements());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        result.put("hasNext", dynamicPage.hasNext());

        return result;
    }

    @Transactional
    public Dynamic publishDynamic(Long userId, String content, List<String> imageUrls) {
        Dynamic dynamic = new Dynamic();
        dynamic.setUserId(userId);
        dynamic.setContent(content);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            try {
                dynamic.setImageUrls(objectMapper.writeValueAsString(imageUrls));
            } catch (Exception e) {
                throw new RuntimeException("图片URL序列化失败", e);
            }
        }

        return dynamicRepository.save(dynamic);
    }

    @Transactional
    public Map<String, Object> toggleLike(Long dynamicId, Long userId) {
        Dynamic dynamic = dynamicRepository.findByIdAndIsDeletedFalseAndSceneType(dynamicId, SCENE_TYPE_FELLOWSHIP);
        if (dynamic == null) {
            throw new RuntimeException("动态不存在");
        }

        boolean isLiked = dynamicLikeRepository.existsByDynamicIdAndUserId(dynamicId, userId);

        if (isLiked) {
            dynamicLikeRepository.deleteByDynamicIdAndUserId(dynamicId, userId);
            dynamic.setLikeCount(Math.max(0, dynamic.getLikeCount() - 1));
        } else {
            DynamicLike like = new DynamicLike();
            like.setDynamicId(dynamicId);
            like.setUserId(userId);
            dynamicLikeRepository.save(like);
            dynamic.setLikeCount(dynamic.getLikeCount() + 1);
            if (!dynamic.getUserId().equals(userId)) {
                User actor = userRepository.findById(userId).orElse(null);
                String actorName = actor != null && actor.getUsername() != null ? actor.getUsername() : "有人";
                try {
                    notificationService.createNotification(
                            dynamic.getUserId(),
                            NotificationCatalog.TYPE_CONTENT_LIKED,
                            actorName + " 点赞了你的动态",
                            actorName + " 点赞了你的动态",
                            "/fellowship/dynamic",
                            "DYNAMIC",
                            String.valueOf(dynamicId));
                } catch (Exception ignored) {
                }
            }
        }

        dynamicRepository.save(dynamic);

        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", !isLiked);
        result.put("likeCount", dynamic.getLikeCount());

        return result;
    }

    @Transactional
    public void deleteDynamic(Long dynamicId, Long userId) {
        Dynamic dynamic = dynamicRepository.findByIdAndIsDeletedFalseAndSceneType(dynamicId, SCENE_TYPE_FELLOWSHIP);
        if (dynamic == null) {
            throw new RuntimeException("动态不存在");
        }

        if (!dynamic.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此动态");
        }

        dynamic.setIsDeleted(true);
        dynamicRepository.save(dynamic);
    }

    public Dynamic getDynamicDetail(Long dynamicId, Long currentUserId) {
        Dynamic dynamic = dynamicRepository.findByIdAndIsDeletedFalseAndSceneType(dynamicId, SCENE_TYPE_FELLOWSHIP);
        if (dynamic == null) {
            throw new RuntimeException("动态不存在");
        }

        return enrichDynamicInfo(dynamic, currentUserId);
    }

    public Long getMyDynamicCount(Long userId) {
        return dynamicRepository.countByUserIdAndIsDeletedFalseAndSceneType(userId, SCENE_TYPE_FELLOWSHIP);
    }

    public Map<String, Object> listDynamicComments(Long dynamicId, int pageNum, int pageSize) {
        Dynamic dynamic = dynamicRepository.findByIdAndIsDeletedFalseAndSceneType(dynamicId, SCENE_TYPE_FELLOWSHIP);
        if (dynamic == null) {
            throw new RuntimeException("动态不存在");
        }
        int safePage = Math.max(1, pageNum);
        int safeSize = Math.min(100, Math.max(1, pageSize));
        Page<DynamicComment> pageResult = dynamicCommentRepository.findByDynamicIdAndStatusOrderByCreatedAtAsc(
                dynamicId, "published", PageRequest.of(safePage - 1, safeSize));
        List<DynamicComment> comments = pageResult.getContent();
        Set<Long> userIds = comments.stream().map(DynamicComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));

        List<Map<String, Object>> items = comments.stream().map(c -> {
            User u = userMap.get(c.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId());
            item.put("userId", c.getUserId());
            item.put("content", c.getContent());
            item.put("createdAt", c.getCreatedAt());
            item.put("authorName", u != null && u.getUsername() != null ? u.getUsername() : "用户");
            item.put("authorAvatarUrl", u != null && u.getProfilePhoto() != null ? u.getProfilePhoto() : "");
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("total", pageResult.getTotalElements());
        result.put("page", safePage);
        result.put("pageSize", safeSize);
        return result;
    }

    @Transactional
    public Map<String, Object> addDynamicComment(Long dynamicId, Long userId, String content) {
        if (content == null || content.isBlank()) {
            throw new RuntimeException("评论内容不能为空");
        }
        String text = content.trim();
        if (text.length() > 500) {
            throw new RuntimeException("评论最多 500 字");
        }

        Dynamic dynamic = dynamicRepository.findByIdAndIsDeletedFalseAndSceneType(dynamicId, SCENE_TYPE_FELLOWSHIP);
        if (dynamic == null) {
            throw new RuntimeException("动态不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        DynamicComment comment = new DynamicComment();
        comment.setDynamicId(dynamicId);
        comment.setUserId(userId);
        comment.setContent(text);
        comment.setStatus("published");
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);
        dynamicCommentRepository.save(comment);

        dynamic.setCommentCount((dynamic.getCommentCount() == null ? 0 : dynamic.getCommentCount()) + 1);
        dynamicRepository.save(dynamic);

        User self = userRepository.findById(userId).orElse(null);
        if (!dynamic.getUserId().equals(userId)) {
            String actorName = self != null && self.getUsername() != null ? self.getUsername() : "有人";
            String preview = text.length() > 50 ? text.substring(0, 50) + "…" : text;
            try {
                notificationService.createNotification(
                        dynamic.getUserId(),
                        NotificationCatalog.TYPE_CONTENT_COMMENTED,
                        actorName + " 评论了你的动态",
                        actorName + "：" + preview,
                        "/fellowship/dynamic",
                        "DYNAMIC",
                        String.valueOf(dynamicId));
            } catch (Exception ignored) {
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", comment.getId());
        result.put("userId", userId);
        result.put("content", comment.getContent());
        result.put("createdAt", comment.getCreatedAt());
        result.put("authorName", self != null && self.getUsername() != null ? self.getUsername() : "用户");
        result.put("authorAvatarUrl", self != null && self.getProfilePhoto() != null ? self.getProfilePhoto() : "");
        result.put("commentCount", dynamic.getCommentCount());
        return result;
    }

    @Transactional
    public void deleteDynamicComment(Long dynamicId, Long commentId, Long userId) {
        Dynamic dynamic = dynamicRepository.findByIdAndIsDeletedFalseAndSceneType(dynamicId, SCENE_TYPE_FELLOWSHIP);
        if (dynamic == null) {
            throw new RuntimeException("动态不存在");
        }
        DynamicComment comment = dynamicCommentRepository.findById(commentId).orElse(null);
        if (comment == null
                || !comment.getDynamicId().equals(dynamicId)
                || !"published".equals(comment.getStatus())) {
            throw new RuntimeException("评论不存在");
        }
        boolean isCommentAuthor = comment.getUserId().equals(userId);
        boolean isPostAuthor = dynamic.getUserId().equals(userId);
        if (!isCommentAuthor && !isPostAuthor) {
            throw new RuntimeException("无权限删除此评论");
        }

        comment.setStatus("deleted");
        comment.setUpdatedAt(LocalDateTime.now());
        dynamicCommentRepository.save(comment);
        dynamic.setCommentCount(Math.max(0, (dynamic.getCommentCount() == null ? 0 : dynamic.getCommentCount()) - 1));
        dynamicRepository.save(dynamic);
    }

    private Dynamic enrichDynamicInfo(Dynamic dynamic, Long currentUserId) {
        User user = userRepository.findById(dynamic.getUserId()).orElse(null);
        if (user != null) {
            dynamic.setUsername(user.getUsername());
            dynamic.setUserAvatar(user.getProfilePhoto());
        }

        if (currentUserId != null) {
            boolean isLiked = dynamicLikeRepository.existsByDynamicIdAndUserId(dynamic.getId(), currentUserId);
            dynamic.setIsLiked(isLiked);
        }

        if (dynamic.getImageUrls() != null && !dynamic.getImageUrls().isEmpty()) {
            try {
                List<String> images = objectMapper.readValue(dynamic.getImageUrls(), new TypeReference<List<String>>() {});
                dynamic.setImages(images);
            } catch (Exception e) {
                dynamic.setImages(List.of());
            }
        }

        return dynamic;
    }
}