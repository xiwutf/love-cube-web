package com.lovecube.backend.services;

import com.lovecube.backend.entity.PositiveShare;
import com.lovecube.backend.entity.PositiveShareComment;
import com.lovecube.backend.entity.PositiveShareLike;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.PositiveShareCommentRepository;
import com.lovecube.backend.repository.PositiveShareLikeRepository;
import com.lovecube.backend.repository.PositiveShareRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PositiveShareService {
    private static final Set<String> ALLOWED_CATEGORIES = Set.of("正能量", "感恩", "鼓励", "成长思考", "信仰感悟", "其他");
    private static final Set<String> ALLOWED_STATUS = Set.of("PUBLISHED", "PENDING", "REJECTED", "DELETED");

    private final PositiveShareRepository positiveShareRepository;
    private final PositiveShareLikeRepository positiveShareLikeRepository;
    private final PositiveShareCommentRepository positiveShareCommentRepository;
    private final UserRepository userRepository;

    public PositiveShareService(
            PositiveShareRepository positiveShareRepository,
            PositiveShareLikeRepository positiveShareLikeRepository,
            PositiveShareCommentRepository positiveShareCommentRepository,
            UserRepository userRepository
    ) {
        this.positiveShareRepository = positiveShareRepository;
        this.positiveShareLikeRepository = positiveShareLikeRepository;
        this.positiveShareCommentRepository = positiveShareCommentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Map<String, Object> createShare(User user, Map<String, Object> payload) {
        String content = String.valueOf(payload.getOrDefault("content", "")).trim();
        if (content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "分享内容不能为空");
        }
        if (content.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "分享内容不能超过1000字");
        }

        String category = String.valueOf(payload.getOrDefault("category", "正能量")).trim();
        if (!ALLOWED_CATEGORIES.contains(category)) {
            category = "其他";
        }

        PositiveShare share = new PositiveShare();
        share.setUserId(user.getUserid());
        share.setContent(content);
        share.setCategory(category);
        share.setAnonymous(Boolean.TRUE.equals(payload.get("anonymous")));
        Object publicVisible = payload.get("publicVisible");
        share.setPublicVisible(publicVisible == null || Boolean.TRUE.equals(publicVisible));
        // 普通用户发布先进入待审核，后续由管理员操作状态流转。
        share.setStatus("PENDING");
        share.setEncourageCount(0);
        share.setCommentCount(0);
        PositiveShare saved = positiveShareRepository.save(share);
        return buildShareItem(saved, user.getUserid(), false, Map.of(user.getUserid(), user));
    }

    public Map<String, Object> listShares(String tab, int page, int pageSize, Long currentUserId) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 50);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize);

        Page<PositiveShare> sharePage;
        String tabKey = tab == null ? "latest" : tab.trim().toLowerCase();
        switch (tabKey) {
            case "today" -> sharePage = positiveShareRepository.findTodayPublic(LocalDate.now().atStartOfDay(), pageable);
            case "hot" -> sharePage = positiveShareRepository.findHotPublic(pageable);
            case "my" -> {
                if (currentUserId == null) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录后查看我的每日心声");
                }
                sharePage = positiveShareRepository.findMyShares(currentUserId, pageable);
            }
            default -> sharePage = positiveShareRepository.findLatestPublic(pageable);
        }

        return buildSharePageResponse(sharePage, currentUserId, safePage, safeSize);
    }

    public Map<String, Object> listMyFavorites(Long userId, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 50);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize);
        Page<PositiveShareLike> likesPage = positiveShareLikeRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<Long> shareIds = likesPage.getContent().stream()
                .map(PositiveShareLike::getShareId)
                .toList();
        if (shareIds.isEmpty()) {
            return Map.of(
                    "list", List.of(),
                    "pageNum", safePage,
                    "pageSize", safeSize,
                    "total", likesPage.getTotalElements(),
                    "hasMore", safePage * safeSize < likesPage.getTotalElements()
            );
        }
        Page<PositiveShare> sharePage = positiveShareRepository.findByIdInAndNotDeleted(shareIds, pageable);
        return buildSharePageResponse(sharePage, userId, safePage, safeSize);
    }

    public Map<String, Object> listMyDrafts(Long userId, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 50);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize);
        Page<PositiveShare> sharePage = positiveShareRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(
                userId,
                List.of("PENDING", "REJECTED"),
                pageable
        );
        return buildSharePageResponse(sharePage, userId, safePage, safeSize);
    }

    @Transactional
    public Map<String, Object> likeShare(Long shareId, Long userId) {
        PositiveShare share = requirePublishedShare(shareId);
        boolean exists = positiveShareLikeRepository.existsByShareIdAndUserId(shareId, userId);
        if (!exists) {
            PositiveShareLike like = new PositiveShareLike();
            like.setShareId(shareId);
            like.setUserId(userId);
            positiveShareLikeRepository.save(like);
            share.setEncourageCount((share.getEncourageCount() == null ? 0 : share.getEncourageCount()) + 1);
            positiveShareRepository.save(share);
        }
        return Map.of(
                "liked", true,
                "alreadyLiked", exists,
                "encourageCount", share.getEncourageCount() == null ? 0 : share.getEncourageCount()
        );
    }

    @Transactional
    public Map<String, Object> unlikeShare(Long shareId, Long userId) {
        PositiveShare share = requirePublishedShare(shareId);
        long deleted = positiveShareLikeRepository.deleteByShareIdAndUserId(shareId, userId);
        if (deleted > 0) {
            int current = share.getEncourageCount() == null ? 0 : share.getEncourageCount();
            share.setEncourageCount(Math.max(current - 1, 0));
            positiveShareRepository.save(share);
        }
        return Map.of(
                "liked", false,
                "encourageCount", share.getEncourageCount() == null ? 0 : share.getEncourageCount()
        );
    }

    @Transactional
    public Map<String, Object> commentShare(Long shareId, Long userId, String content) {
        PositiveShare share = requirePublishedShare(shareId);
        String cleanContent = content == null ? "" : content.trim();
        if (cleanContent.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "留言内容不能为空");
        }
        if (cleanContent.length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "留言内容不能超过500字");
        }
        PositiveShareComment comment = new PositiveShareComment();
        comment.setShareId(shareId);
        comment.setUserId(userId);
        comment.setContent(cleanContent);
        PositiveShareComment saved = positiveShareCommentRepository.save(comment);
        share.setCommentCount((share.getCommentCount() == null ? 0 : share.getCommentCount()) + 1);
        positiveShareRepository.save(share);
        User user = userRepository.findById(userId).orElse(null);
        return Map.of(
                "id", saved.getId(),
                "content", saved.getContent(),
                "createdAt", saved.getCreatedAt(),
                "userId", userId,
                "username", user != null ? user.getUsername() : "用户",
                "avatar", user != null ? user.getProfilePhoto() : null,
                "commentCount", share.getCommentCount()
        );
    }

    public Map<String, Object> listShareComments(Long shareId, int page, int pageSize, Long currentUserId) {
        PositiveShare share = positiveShareRepository.findById(shareId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "内容不存在"));
        boolean canRead = ("PUBLISHED".equals(share.getStatus()) && Boolean.TRUE.equals(share.getPublicVisible()))
                || (currentUserId != null && currentUserId.equals(share.getUserId()));
        if (!canRead) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "暂无权限查看评论");
        }
        return listShareCommentsInternal(shareId, page, pageSize);
    }

    public Map<String, Object> listShareCommentsForAdmin(Long shareId, int page, int pageSize) {
        positiveShareRepository.findById(shareId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "内容不存在"));
        return listShareCommentsInternal(shareId, page, pageSize);
    }

    public Map<String, Object> listSharesForAdmin(String status, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 50);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize);
        Page<PositiveShare> sharePage;
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            sharePage = positiveShareRepository.findAllNonDeleted(pageable);
        } else {
            String normalized = status.trim().toUpperCase(Locale.ROOT);
            if (!ALLOWED_STATUS.contains(normalized)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status 不合法");
            }
            sharePage = positiveShareRepository.findByStatusOrderByCreatedAtDesc(normalized, pageable);
        }

        List<PositiveShare> records = sharePage.getContent();
        List<Long> userIds = records.stream().map(PositiveShare::getUserId).distinct().toList();
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));
        List<Map<String, Object>> list = new ArrayList<>();
        for (PositiveShare record : records) {
            list.add(buildShareItem(record, record.getUserId(), false, userMap));
        }

        return Map.of(
                "list", list,
                "pageNum", safePage,
                "pageSize", safeSize,
                "total", sharePage.getTotalElements(),
                "hasMore", safePage * safeSize < sharePage.getTotalElements()
        );
    }

    @Transactional
    public Map<String, Object> reviewShare(Long shareId, String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("PUBLISHED", "REJECTED", "DELETED", "PENDING").contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "审核状态不合法");
        }
        PositiveShare share = positiveShareRepository.findById(shareId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "内容不存在"));
        share.setStatus(normalized);
        positiveShareRepository.save(share);
        return Map.of(
                "id", share.getId(),
                "status", share.getStatus(),
                "message", "状态已更新"
        );
    }

    @Transactional
    public Map<String, Object> batchReviewShares(List<Long> shareIds, String status) {
        if (shareIds == null || shareIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请至少选择一条内容");
        }
        String normalized = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("PUBLISHED", "REJECTED", "DELETED", "PENDING").contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "审核状态不合法");
        }
        List<PositiveShare> shares = positiveShareRepository.findAllById(shareIds);
        if (shares.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "未找到可操作内容");
        }
        for (PositiveShare share : shares) {
            share.setStatus(normalized);
        }
        positiveShareRepository.saveAll(shares);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("updatedCount", shares.size());
        result.put("status", normalized);
        result.put("message", "批量审核完成");
        return result;
    }

    private PositiveShare requirePublishedShare(Long shareId) {
        PositiveShare share = positiveShareRepository.findById(shareId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "内容不存在"));
        if (!"PUBLISHED".equals(share.getStatus()) || !Boolean.TRUE.equals(share.getPublicVisible())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该内容不可互动");
        }
        return share;
    }

    private Set<Long> resolveLikedShareIds(List<PositiveShare> shares, Long currentUserId) {
        if (currentUserId == null || shares.isEmpty()) {
            return Set.of();
        }
        List<Long> shareIds = shares.stream().map(PositiveShare::getId).toList();
        return new HashSet<>(positiveShareLikeRepository.findByShareIdInAndUserId(shareIds, currentUserId)
                .stream()
                .map(PositiveShareLike::getShareId)
                .toList());
    }

    private Map<String, Object> buildSharePageResponse(
            Page<PositiveShare> sharePage,
            Long currentUserId,
            int safePage,
            int safeSize
    ) {
        List<PositiveShare> records = sharePage.getContent();
        List<Long> userIds = records.stream().map(PositiveShare::getUserId).distinct().toList();
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));
        Set<Long> likedShareIds = resolveLikedShareIds(records, currentUserId);
        List<Map<String, Object>> items = new ArrayList<>();
        for (PositiveShare record : records) {
            items.add(buildShareItem(record, currentUserId, likedShareIds.contains(record.getId()), userMap));
        }
        return Map.of(
                "list", items,
                "pageNum", safePage,
                "pageSize", safeSize,
                "total", sharePage.getTotalElements(),
                "hasMore", safePage * safeSize < sharePage.getTotalElements()
        );
    }

    private Map<String, Object> listShareCommentsInternal(Long shareId, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 50);
        Page<PositiveShareComment> commentPage = positiveShareCommentRepository.findByShareIdOrderByCreatedAtDesc(
                shareId,
                PageRequest.of(safePage - 1, safeSize)
        );
        List<PositiveShareComment> rows = commentPage.getContent();
        List<Long> userIds = rows.stream().map(PositiveShareComment::getUserId).distinct().toList();
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));
        List<Map<String, Object>> comments = new ArrayList<>();
        for (PositiveShareComment row : rows) {
            User user = userMap.get(row.getUserId());
            comments.add(Map.of(
                    "id", row.getId(),
                    "shareId", row.getShareId(),
                    "userId", row.getUserId(),
                    "username", user != null && user.getUsername() != null && !user.getUsername().isBlank() ? user.getUsername() : "平台用户",
                    "avatar", user != null ? user.getProfilePhoto() : null,
                    "content", row.getContent(),
                    "createdAt", row.getCreatedAt()
            ));
        }
        return Map.of(
                "list", comments,
                "pageNum", safePage,
                "pageSize", safeSize,
                "total", commentPage.getTotalElements(),
                "hasMore", safePage * safeSize < commentPage.getTotalElements()
        );
    }

    private Map<String, Object> buildShareItem(
            PositiveShare share,
            Long currentUserId,
            boolean liked,
            Map<Long, User> userMap
    ) {
        User author = userMap.get(share.getUserId());
        String displayName;
        if (Boolean.TRUE.equals(share.getAnonymous()) && (currentUserId == null || !currentUserId.equals(share.getUserId()))) {
            displayName = "匿名用户";
        } else {
            displayName = author != null && author.getUsername() != null && !author.getUsername().isBlank()
                    ? author.getUsername()
                    : "平台用户";
        }
        String avatar = null;
        if (!Boolean.TRUE.equals(share.getAnonymous()) || (currentUserId != null && currentUserId.equals(share.getUserId()))) {
            avatar = author != null ? author.getProfilePhoto() : null;
        }
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", share.getId());
        item.put("userId", share.getUserId());
        item.put("nickname", displayName);
        item.put("avatar", avatar);
        item.put("category", share.getCategory());
        item.put("content", share.getContent());
        item.put("anonymous", Boolean.TRUE.equals(share.getAnonymous()));
        item.put("publicVisible", Boolean.TRUE.equals(share.getPublicVisible()));
        item.put("status", share.getStatus());
        item.put("encourageCount", share.getEncourageCount() == null ? 0 : share.getEncourageCount());
        item.put("commentCount", share.getCommentCount() == null ? 0 : share.getCommentCount());
        item.put("createdAt", share.getCreatedAt());
        item.put("liked", liked);
        item.put("mine", currentUserId != null && currentUserId.equals(share.getUserId()));
        return item;
    }
}
