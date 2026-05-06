package com.lovecube.backend.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovecube.backend.entity.Dynamic;
import com.lovecube.backend.entity.DynamicLike;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.DynamicLikeRepository;
import com.lovecube.backend.repository.DynamicRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 为 {@link com.lovecube.backend.controllers.AdminFellowshipDynamicController} 提供联谊动态与点赞分页数据。
 * 列表与点赞行字段见 Controller 类级 JavaDoc；{@code rows} 与 {@code list} 为同一列表引用。
 * 作者/点赞用户 ID 为空或非法时跳过批量查询并在行内展示兜底文案，避免脏数据导致 500。
 */
@Service
public class AdminFellowshipDynamicService {

    private final DynamicRepository dynamicRepository;
    private final DynamicLikeRepository dynamicLikeRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdminFellowshipDynamicService(
            DynamicRepository dynamicRepository,
            DynamicLikeRepository dynamicLikeRepository,
            UserRepository userRepository) {
        this.dynamicRepository = dynamicRepository;
        this.dynamicLikeRepository = dynamicLikeRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> listDynamics(int pageNum, int pageSize) {
        int pn = Math.max(1, pageNum);
        int ps = Math.min(50, Math.max(1, pageSize));
        Pageable pageable = PageRequest.of(pn - 1, ps);
        Page<Dynamic> page = dynamicRepository.findByIsDeletedFalseOrderByCreatedAtDesc(pageable);

        Set<Long> userIds = page.getContent().stream()
                .map(Dynamic::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(HashSet::new));
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userRepository.findAllById(userIds).forEach(u -> userMap.put(u.getUserid(), u));
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Dynamic d : page.getContent()) {
            list.add(toDynamicRow(d, userMap.get(d.getUserId())));
        }

        Map<String, Object> out = new HashMap<>();
        out.put("list", list);
        out.put("rows", list);
        out.put("total", page.getTotalElements());
        out.put("pageNum", pn);
        out.put("pageSize", ps);
        out.put("hasNext", page.hasNext());
        return out;
    }

    public Map<String, Object> listLikesForDynamic(Long dynamicId, int pageNum, int pageSize) {
        dynamicRepository.findById(dynamicId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "动态不存在"));

        int pn = Math.max(1, pageNum);
        int ps = Math.min(100, Math.max(1, pageSize));
        Pageable pageable = PageRequest.of(pn - 1, ps);
        Page<DynamicLike> likePage = dynamicLikeRepository.findByDynamicIdOrderByCreatedAtDesc(dynamicId, pageable);

        Set<Long> userIds = likePage.getContent().stream()
                .map(DynamicLike::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(HashSet::new));
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userRepository.findAllById(userIds).forEach(u -> userMap.put(u.getUserid(), u));
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (DynamicLike like : likePage.getContent()) {
            User u = userMap.get(like.getUserId());
            Map<String, Object> row = new HashMap<>();
            row.put("id", like.getId());
            row.put("userId", like.getUserId());
            row.put("likedUserId", like.getUserId());
            String likeUserName = u != null && u.getUsername() != null && !u.getUsername().isBlank()
                    ? u.getUsername()
                    : (like.getUserId() == null ? "未知用户" : ("用户" + like.getUserId()));
            row.put("username", likeUserName);
            row.put("likedUsername", likeUserName);
            row.put("userAvatar", u != null ? u.getProfilePhoto() : null);
            row.put("likedUserAvatar", u != null ? u.getProfilePhoto() : null);
            row.put("createdAt", like.getCreatedAt());
            row.put("likedAt", like.getCreatedAt());
            row.put("fellowshipDynamicId", dynamicId);
            list.add(row);
        }

        Map<String, Object> out = new HashMap<>();
        out.put("list", list);
        out.put("rows", list);
        out.put("total", likePage.getTotalElements());
        out.put("pageNum", pn);
        out.put("pageSize", ps);
        out.put("hasNext", likePage.hasNext());
        return out;
    }

    private Map<String, Object> toDynamicRow(Dynamic d, User author) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", d.getId());
        row.put("fellowshipDynamicId", d.getId());
        row.put("userId", d.getUserId());
        row.put("authorId", d.getUserId());
        String authorName = author != null && author.getUsername() != null && !author.getUsername().isBlank()
                ? author.getUsername()
                : (d.getUserId() == null ? "未知用户" : ("用户" + d.getUserId()));
        row.put("username", authorName);
        row.put("authorName", authorName);
        row.put("userAvatar", author != null ? author.getProfilePhoto() : null);
        row.put("authorAvatar", author != null ? author.getProfilePhoto() : null);
        row.put("content", d.getContent() == null ? "" : d.getContent());
        row.put("likeCount", d.getLikeCount() != null ? d.getLikeCount() : 0);
        row.put("commentCount", d.getCommentCount() != null ? d.getCommentCount() : 0);
        row.put("createdAt", d.getCreatedAt());
        row.put("publishedAt", d.getCreatedAt());
        row.put("images", parseImages(d.getImageUrls()));
        return row;
    }

    private List<String> parseImages(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
