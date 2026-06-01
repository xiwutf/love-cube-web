package com.lovecube.backend.services;

import com.lovecube.backend.entity.InterestTopic;
import com.lovecube.backend.entity.InterestTopicPost;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.InterestTopicPostRepository;
import com.lovecube.backend.repository.InterestTopicRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterestTopicService {

    private final InterestTopicRepository topicRepository;
    private final InterestTopicPostRepository postRepository;
    private final UserRepository userRepository;

    public InterestTopicService(
            InterestTopicRepository topicRepository,
            InterestTopicPostRepository postRepository,
            UserRepository userRepository
    ) {
        this.topicRepository = topicRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public List<Map<String, Object>> listTopics() {
        return topicRepository.findByEnabledOrderBySortNoAscHeatDesc(1).stream()
                .map(this::toTopicRow)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getTopicDetail(Long topicId, int page, int size) {
        InterestTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "话题不存在"));
        if (!Integer.valueOf(1).equals(topic.getEnabled())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "话题不存在");
        }
        int safeSize = Math.min(Math.max(size, 1), 30);
        int safePage = Math.max(page, 1) - 1;
        List<InterestTopicPost> posts = postRepository.findByTopicIdAndStatusOrderByCreatedAtDesc(
                topicId, "published", PageRequest.of(safePage, safeSize));
        Set<Long> userIds = posts.stream().map(InterestTopicPost::getUserId).collect(Collectors.toSet());
        Map<Long, User> users = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u, (a, b) -> a));

        List<Map<String, Object>> postRows = new ArrayList<>();
        for (InterestTopicPost post : posts) {
            User author = users.get(post.getUserId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", post.getId());
            row.put("content", post.getContent());
            row.put("likeCount", post.getLikeCount());
            row.put("createdAt", post.getCreatedAt());
            row.put("authorName", author != null && author.getUsername() != null ? author.getUsername() : "用户");
            row.put("authorAvatar", author != null ? author.getProfilePhoto() : null);
            postRows.add(row);
        }

        Map<String, Object> body = toTopicRow(topic);
        body.put("posts", postRows);
        body.put("page", safePage + 1);
        body.put("size", safeSize);
        return body;
    }

    @Transactional
    public Map<String, Object> createPost(User user, Long topicId, String content) {
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "内容不能为空");
        }
        String trimmed = content.trim();
        if (trimmed.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "内容过长");
        }
        InterestTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "话题不存在"));

        InterestTopicPost post = new InterestTopicPost();
        post.setTopicId(topicId);
        post.setUserId(user.getUserid());
        post.setContent(trimmed);
        postRepository.save(post);

        topic.setPostCount((int) postRepository.countByTopicIdAndStatus(topicId, "published"));
        topic.setHeat((topic.getHeat() == null ? 0 : topic.getHeat()) + 1);
        topicRepository.save(topic);

        return Map.of("id", post.getId(), "message", "发布成功");
    }

    private Map<String, Object> toTopicRow(InterestTopic topic) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", topic.getId());
        row.put("title", topic.getTitle());
        row.put("description", topic.getDescription());
        row.put("postCount", topic.getPostCount());
        row.put("heat", topic.getHeat());
        return row;
    }
}
