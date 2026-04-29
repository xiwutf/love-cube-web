package com.lovecube.backend.services;

import com.lovecube.backend.entity.OperationPost;
import com.lovecube.backend.repository.OperationPostRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OperationPostService {

    private static final String STATUS_PUBLISHED = "published";
    private static final int MAX_LIMIT = 50;

    private final OperationPostRepository repository;

    public OperationPostService(OperationPostRepository repository) {
        this.repository = repository;
    }

    public List<OperationPost> getPosts(String type, String scope, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
        PageRequest page = PageRequest.of(0, safeLimit);
        boolean hasType = type != null && !type.isBlank();
        boolean hasScope = scope != null && !scope.isBlank();
        if (hasType && hasScope) {
            return repository.findByTypeAndScope(STATUS_PUBLISHED, type, scope, page);
        } else if (hasType) {
            return repository.findByType(STATUS_PUBLISHED, type, page);
        } else if (hasScope) {
            return repository.findByScope(STATUS_PUBLISHED, scope, page);
        }
        return repository.findPublished(STATUS_PUBLISHED, page);
    }

    public OperationPost getLatest() {
        List<OperationPost> results = repository.findLatestPublished(PageRequest.of(0, 1));
        return results.isEmpty() ? null : results.get(0);
    }

    public OperationPost getById(String id) {
        return repository.findById(id)
                .filter(p -> STATUS_PUBLISHED.equals(p.getStatus()))
                .orElse(null);
    }

    @Transactional
    public OperationPost create(OperationPost post) {
        post.setId(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        if (post.getIsTop() == null) post.setIsTop(false);
        if (post.getScope() == null) post.setScope("all");
        if (post.getStatus() == null) post.setStatus("draft");
        if (STATUS_PUBLISHED.equals(post.getStatus()) && post.getPublishTime() == null) {
            post.setPublishTime(now);
        }
        return repository.save(post);
    }

    @Transactional
    public OperationPost update(String id, OperationPost patch) {
        OperationPost existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("运营内容不存在: " + id));
        if (patch.getTitle() != null) existing.setTitle(patch.getTitle());
        if (patch.getSummary() != null) existing.setSummary(patch.getSummary());
        if (patch.getContent() != null) existing.setContent(patch.getContent());
        if (patch.getType() != null) existing.setType(patch.getType());
        if (patch.getScope() != null) existing.setScope(patch.getScope());
        if (patch.getStatus() != null) existing.setStatus(patch.getStatus());
        if (patch.getIsTop() != null) existing.setIsTop(patch.getIsTop());
        if (patch.getPublishTime() != null) existing.setPublishTime(patch.getPublishTime());
        if (STATUS_PUBLISHED.equals(patch.getStatus()) && existing.getPublishTime() == null) {
            existing.setPublishTime(LocalDateTime.now());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        return repository.save(existing);
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("运营内容不存在: " + id);
        }
        repository.deleteById(id);
    }

    public List<OperationPost> getAll() {
        return repository.findAll();
    }
}
