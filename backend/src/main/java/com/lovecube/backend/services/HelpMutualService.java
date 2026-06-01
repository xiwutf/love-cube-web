package com.lovecube.backend.services;

import com.lovecube.backend.entity.HelpReply;
import com.lovecube.backend.entity.HelpRequest;
import com.lovecube.backend.entity.UserHelpStats;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.HelpReplyRepository;
import com.lovecube.backend.repository.HelpRequestRepository;
import com.lovecube.backend.repository.UserHelpStatsRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.util.HelpSensitiveTextValidator;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HelpMutualService {

    public static final Set<String> ALLOWED_HELP_TYPES = Set.of(
            "JOB_SEEK", "RECRUIT", "FIND_MATERIAL", "OFFER_RESOURCE", "ASK_EXP", "OTHER"
    );

    private final HelpRequestRepository helpRequestRepository;
    private final HelpReplyRepository helpReplyRepository;
    private final UserHelpStatsRepository userHelpStatsRepository;
    private final UserRepository userRepository;

    public HelpMutualService(
            HelpRequestRepository helpRequestRepository,
            HelpReplyRepository helpReplyRepository,
            UserHelpStatsRepository userHelpStatsRepository,
            UserRepository userRepository
    ) {
        this.helpRequestRepository = helpRequestRepository;
        this.helpReplyRepository = helpReplyRepository;
        this.userHelpStatsRepository = userHelpStatsRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Map<String, Object> createRequest(User user, Map<String, Object> payload) {
        String helpType = normalizeHelpType(String.valueOf(payload.getOrDefault("helpType", "")));
        String title = String.valueOf(payload.getOrDefault("title", "")).trim();
        String content = String.valueOf(payload.getOrDefault("content", "")).trim();
        String region = String.valueOf(payload.getOrDefault("region", "")).trim();
        String contactType = String.valueOf(payload.getOrDefault("contactType", "")).trim();
        String contactValue = String.valueOf(payload.getOrDefault("contactValue", "")).trim();
        boolean contactPublic = Boolean.TRUE.equals(payload.get("contactPublic"));

        try {
            HelpSensitiveTextValidator.validateBody("标题", title, 200);
            HelpSensitiveTextValidator.validateBody("需求描述", content, 5000);
            HelpSensitiveTextValidator.validateOptional("地区", region, 100);
            HelpSensitiveTextValidator.validateOptional("联系方式内容", contactValue, 200);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        if (!contactType.isEmpty()) {
            try {
                HelpSensitiveTextValidator.validateBody("联系方式类型", contactType, 32);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }

        HelpRequest r = new HelpRequest();
        r.setUserId(user.getUserid());
        r.setHelpType(helpType);
        r.setTitle(title);
        r.setContent(content);
        r.setRegion(region.isEmpty() ? null : region);
        r.setContactType(contactType.isEmpty() ? null : contactType);
        r.setContactValue(contactValue.isEmpty() ? null : contactValue);
        r.setContactPublic(contactPublic);
        r.setDeadline(parseDeadline(payload.get("deadline")));
        r.setStatus("pending");
        r.setReplyCount(0);
        HelpRequest saved = helpRequestRepository.save(r);
        return toRequestMap(saved, resolveUserMap(List.of(saved.getUserId())), null, false);
    }

    public Map<String, Object> listPublic(String helpType, int pageNum, int pageSize) {
        int page = Math.max(pageNum, 1);
        int size = Math.min(Math.max(pageSize, 1), 50);
        Pageable pageable = PageRequest.of(page - 1, size);
        String typeFilter = normalizeListTypeFilter(helpType);
        Page<HelpRequest> p = helpRequestRepository.pageActivePublic(typeFilter, pageable);
        Map<Long, User> users = resolveUserMap(p.getContent().stream().map(HelpRequest::getUserId).distinct().toList());
        List<Map<String, Object>> list = p.getContent().stream()
                .map(r -> toRequestMap(r, users, null, false))
                .toList();
        return Map.of(
                "list", list,
                "pageNum", page,
                "pageSize", size,
                "total", p.getTotalElements(),
                "totalPages", p.getTotalPages()
        );
    }

    public Map<String, Object> getRequestDetail(long id, Long viewerUserId) {
        HelpRequest r = helpRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "需求不存在"));
        if (!"active".equals(r.getStatus())) {
            if (viewerUserId == null || !viewerUserId.equals(r.getUserId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "需求不存在或未上架");
            }
        }
        boolean viewerIsAuthor = viewerUserId != null && viewerUserId.equals(r.getUserId());

        Map<Long, User> users = resolveUserMap(collectUserIdsForRequest(r));
        List<HelpReply> replies = helpReplyRepository.findByRequestIdOrderByCreatedAtAsc(id);
        List<Long> replyUserIds = replies.stream().map(HelpReply::getUserId).distinct().toList();
        Map<Long, User> replyUsers = resolveUserMap(replyUserIds);
        users.putAll(replyUsers);

        List<Map<String, Object>> replyMaps = new ArrayList<>();
        for (HelpReply rep : replies) {
            replyMaps.add(toReplyMap(rep, r, viewerUserId, replyUsers));
        }

        Map<String, Object> body = toRequestMap(r, users, viewerUserId, true);
        body.put("replies", replyMaps);
        body.put("viewerIsAuthor", viewerIsAuthor);
        body.put("viewerHasAcceptedOffer",
                viewerUserId != null && replies.stream().anyMatch(
                        x -> viewerUserId.equals(x.getUserId()) && "accepted".equals(x.getStatus())));
        return body;
    }

    public Map<String, Object> listMineAuthored(User user, int pageNum, int pageSize) {
        return pageMyRequests(user.getUserid(), pageNum, pageSize);
    }

    public Map<String, Object> listMineReplied(User user, int pageNum, int pageSize) {
        int page = Math.max(pageNum, 1);
        int size = Math.min(Math.max(pageSize, 1), 50);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<HelpReply> rp = helpReplyRepository.findByUserIdOrderByCreatedAtDesc(user.getUserid(), pageable);
        List<Long> reqIds = rp.getContent().stream().map(HelpReply::getRequestId).distinct().toList();
        List<HelpRequest> reqs = reqIds.isEmpty() ? List.of() : helpRequestRepository.findAllById(reqIds);
        Map<Long, HelpRequest> reqMap = reqs.stream().collect(Collectors.toMap(HelpRequest::getId, x -> x));
        Map<Long, User> users = resolveUserMap(authorAndHelperUserIds(reqs));
        List<Map<String, Object>> list = new ArrayList<>();
        for (HelpReply rep : rp.getContent()) {
            HelpRequest hr = reqMap.get(rep.getRequestId());
            if (hr == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("reply", toReplyMap(rep, hr, user.getUserid(), resolveUserMap(List.of(rep.getUserId()))));
            row.put("request", toRequestMap(hr, users, null, false));
            list.add(row);
        }
        return Map.of(
                "list", list,
                "pageNum", page,
                "pageSize", size,
                "total", rp.getTotalElements(),
                "totalPages", rp.getTotalPages()
        );
    }

    @Transactional
    public Map<String, Object> createReply(long requestId, User user, Map<String, Object> payload) {
        HelpRequest r = helpRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "需求不存在"));
        if (!"active".equals(r.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前需求不可回应");
        }
        if (user.getUserid().equals(r.getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能回应自己发布的需求");
        }
        if (helpReplyRepository.existsByRequestIdAndUserId(requestId, user.getUserid())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "您已提交过互助意向");
        }
        String message = String.valueOf(payload.getOrDefault("message", "")).trim();
        boolean contactWilling = Boolean.TRUE.equals(payload.get("contactWilling"));
        String contactType = String.valueOf(payload.getOrDefault("contactType", "")).trim();
        String contactValue = String.valueOf(payload.getOrDefault("contactValue", "")).trim();
        try {
            HelpSensitiveTextValidator.validateBody("互助说明", message, 2000);
            if (contactWilling) {
                HelpSensitiveTextValidator.validateBody("联系方式", contactValue, 200);
                if (!contactType.isEmpty()) {
                    HelpSensitiveTextValidator.validateBody("联系方式类型", contactType, 32);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        if (contactWilling && contactValue.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请填写联系方式或关闭「愿意提供联系方式」");
        }

        HelpReply rep = new HelpReply();
        rep.setRequestId(requestId);
        rep.setUserId(user.getUserid());
        rep.setMessage(message);
        rep.setContactWilling(contactWilling);
        rep.setContactType(contactWilling && !contactType.isEmpty() ? contactType : null);
        rep.setContactValue(contactWilling ? contactValue : null);
        rep.setStatus("pending");
        rep.setIsHelper(Boolean.FALSE);
        helpReplyRepository.save(rep);

        r.setReplyCount((r.getReplyCount() == null ? 0 : r.getReplyCount()) + 1);
        helpRequestRepository.save(r);

        bumpHelpCount(user.getUserid());

        Map<Long, User> ru = resolveUserMap(List.of(user.getUserid()));
        return toReplyMap(rep, r, user.getUserid(), ru);
    }

    @Transactional
    public Map<String, Object> acceptReply(long replyId, User publisher) {
        HelpReply rep = helpReplyRepository.findById(replyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "互助意向不存在"));
        HelpRequest r = helpRequestRepository.findById(rep.getRequestId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "需求不存在"));
        if (!publisher.getUserid().equals(r.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅发布者可确认");
        }
        if (!"active".equals(r.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "当前状态不可确认");
        }
        if (!"pending".equals(rep.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该意向已处理");
        }
        rep.setStatus("accepted");
        helpReplyRepository.save(rep);
        bumpAcceptedCount(rep.getUserId());
        return toReplyMap(rep, r, publisher.getUserid(), resolveUserMap(List.of(rep.getUserId())));
    }

    /**
     * 发布者拒绝 pending 互助意向；不计入 accepted/success 统计。
     * 成功时返回统一结构：success、message、data（意向详情）。
     */
    @Transactional
    public Map<String, Object> rejectReply(long replyId, User publisher) {
        HelpReply rep = helpReplyRepository.findById(replyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "互助意向不存在"));
        HelpRequest r = helpRequestRepository.findById(rep.getRequestId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "需求不存在"));
        if (!publisher.getUserid().equals(r.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅发布者可拒绝");
        }
        if ("resolved".equals(r.getStatus()) || "closed".equals(r.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "需求已结束，无法再处理互助意向");
        }
        if (!"pending".equals(rep.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "仅待确认状态的意向可拒绝");
        }
        rep.setStatus("rejected");
        helpReplyRepository.save(rep);

        Map<String, Object> data = toReplyMap(rep, r, publisher.getUserid(), resolveUserMap(List.of(rep.getUserId())));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", "已拒绝该互助意向");
        body.put("data", data);
        return body;
    }

    @Transactional
    public Map<String, Object> closeRequest(long requestId, User publisher) {
        HelpRequest r = loadOwnedRequest(requestId, publisher);
        if ("resolved".equals(r.getStatus()) || "closed".equals(r.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "需求已结束");
        }
        if ("rejected".equals(r.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "需求已驳回");
        }
        r.setStatus("closed");
        helpRequestRepository.save(r);
        return toRequestMap(r, resolveUserMap(List.of(r.getUserId())), publisher.getUserid(), true);
    }

    @Transactional
    public Map<String, Object> resolveRequest(long requestId, User publisher, Map<String, Object> payload) {
        HelpRequest r = loadOwnedRequest(requestId, publisher);
        if (!"active".equals(r.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "仅进行中的需求可标记已解决");
        }
        long helperUserId;
        try {
            helperUserId = Long.parseLong(String.valueOf(payload.getOrDefault("helperUserId", "")));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择帮助人");
        }
        String thank = String.valueOf(payload.getOrDefault("thankNote", "")).trim();
        try {
            HelpSensitiveTextValidator.validateOptional("感谢说明", thank, 500);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        HelpReply chosen = helpReplyRepository.findByRequestIdAndUserIdAndStatus(requestId, helperUserId, "accepted")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "帮助人须来自已接受的互助意向"));

        List<HelpReply> all = helpReplyRepository.findByRequestIdOrderByCreatedAtAsc(requestId);
        for (HelpReply x : all) {
            x.setIsHelper(x.getId().equals(chosen.getId()));
            helpReplyRepository.save(x);
        }

        r.setStatus("resolved");
        r.setHelperUserId(helperUserId);
        r.setResolvedAt(LocalDateTime.now());
        r.setResolvedNote(thank.isEmpty() ? null : thank);
        helpRequestRepository.save(r);

        bumpSuccessCount(helperUserId);

        Map<Long, User> users = resolveUserMap(List.of(r.getUserId(), helperUserId));
        return toRequestMap(r, users, publisher.getUserid(), true);
    }

    public Map<String, Object> getUserStats(long userId) {
        UserHelpStats s = userHelpStatsRepository.findById(userId).orElse(null);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId", userId);
        if (s == null) {
            m.put("helpCount", 0);
            m.put("successCount", 0);
            m.put("acceptedCount", 0);
            m.put("creditScore", 0);
        } else {
            m.put("helpCount", s.getHelpCount());
            m.put("successCount", s.getSuccessCount());
            m.put("acceptedCount", s.getAcceptedCount());
            m.put("creditScore", s.getCreditScore() == null ? 0 : s.getCreditScore());
        }
        return m;
    }

    public Map<String, Object> listForAdmin(String status, int pageNum, int pageSize) {
        int page = Math.max(pageNum, 1);
        int size = Math.min(Math.max(pageSize, 1), 100);
        Pageable pageable = PageRequest.of(page - 1, size);
        String st = status == null || "ALL".equalsIgnoreCase(status.trim()) ? "" : status.trim().toLowerCase();
        Page<HelpRequest> p = helpRequestRepository.pageAdmin(st, pageable);
        Map<Long, User> users = resolveUserMap(authorAndHelperUserIds(p.getContent()));
        List<Map<String, Object>> list = p.getContent().stream()
                .map(r -> toRequestMap(r, users, null, true))
                .toList();
        return Map.of(
                "list", list,
                "pageNum", page,
                "pageSize", size,
                "total", p.getTotalElements(),
                "totalPages", p.getTotalPages()
        );
    }

    private static List<Long> authorAndHelperUserIds(List<HelpRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        for (HelpRequest r : requests) {
            if (r.getUserId() != null) {
                ids.add(r.getUserId());
            }
            if (r.getHelperUserId() != null) {
                ids.add(r.getHelperUserId());
            }
        }
        return ids.stream().distinct().toList();
    }

    private static String replyStatusLabelCn(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case "pending" -> "待确认";
            case "accepted" -> "已接受";
            case "rejected" -> "已拒绝";
            default -> status;
        };
    }

    @Transactional
    public Map<String, Object> adminReview(long id, String newStatus) {
        HelpRequest r = helpRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "需求不存在"));
        String s = newStatus == null ? "" : newStatus.trim().toLowerCase();
        if (!"active".equals(s) && !"rejected".equals(s)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "非法审核状态");
        }
        if (!"pending".equals(r.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "仅待审核需求可操作");
        }
        if ("active".equals(s)) {
            try {
                HelpSensitiveTextValidator.assertNoBannedCombined(r.getTitle(), r.getContent());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }
        r.setStatus(s);
        helpRequestRepository.save(r);
        return toRequestMap(r, resolveUserMap(List.of(r.getUserId())), null, true);
    }

    private HelpRequest loadOwnedRequest(long requestId, User publisher) {
        HelpRequest r = helpRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "需求不存在"));
        if (!publisher.getUserid().equals(r.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅发布者可操作");
        }
        return r;
    }

    private Map<String, Object> pageMyRequests(long userId, int pageNum, int pageSize) {
        int page = Math.max(pageNum, 1);
        int size = Math.min(Math.max(pageSize, 1), 50);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<HelpRequest> p = helpRequestRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        Map<Long, User> users = resolveUserMap(authorAndHelperUserIds(p.getContent()));
        List<Map<String, Object>> list = p.getContent().stream()
                .map(r -> toRequestMap(r, users, userId, true))
                .toList();
        return Map.of(
                "list", list,
                "pageNum", page,
                "pageSize", size,
                "total", p.getTotalElements(),
                "totalPages", p.getTotalPages()
        );
    }

    private List<Long> collectUserIdsForRequest(HelpRequest r) {
        List<Long> ids = new ArrayList<>();
        ids.add(r.getUserId());
        if (r.getHelperUserId() != null) {
            ids.add(r.getHelperUserId());
        }
        return ids.stream().distinct().toList();
    }

    private Map<String, Object> toRequestMap(HelpRequest r, Map<Long, User> users, Long viewerUserId, boolean forDetail) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("userId", r.getUserId());
        m.put("helpType", r.getHelpType());
        m.put("title", r.getTitle());
        if (forDetail) {
            m.put("content", r.getContent());
        } else {
            String c = r.getContent() == null ? "" : r.getContent();
            m.put("summary", c.length() > 160 ? c.substring(0, 160) + "…" : c);
        }
        m.put("region", r.getRegion());
        m.put("deadline", r.getDeadline());
        m.put("status", r.getStatus());
        m.put("replyCount", r.getReplyCount());
        m.put("helperUserId", r.getHelperUserId());
        m.put("resolvedAt", r.getResolvedAt());
        m.put("resolvedNote", r.getResolvedNote());
        m.put("isResolved", Boolean.valueOf("resolved".equals(r.getStatus())));
        if (r.getHelperUserId() != null) {
            User helper = users.get(r.getHelperUserId());
            m.put("helperName", helper != null ? displayName(helper) : ("用户" + r.getHelperUserId()));
        } else {
            m.put("helperName", null);
        }
        m.put("contactPublic", Boolean.TRUE.equals(r.getContactPublic()));
        m.put("createdAt", r.getCreatedAt());
        m.put("updatedAt", r.getUpdatedAt());
        User author = users.get(r.getUserId());
        m.put("publisherName", author != null ? displayName(author) : "用户");
        boolean showAuthorContact = false;
        if (viewerUserId != null) {
            if (viewerUserId.equals(r.getUserId())) {
                showAuthorContact = true;
            } else if (Boolean.TRUE.equals(r.getContactPublic())) {
                showAuthorContact = true;
            } else {
                boolean acceptedHelper = helpReplyRepository.findByRequestIdOrderByCreatedAtAsc(r.getId()).stream()
                        .anyMatch(rep -> viewerUserId.equals(rep.getUserId()) && "accepted".equals(rep.getStatus()));
                showAuthorContact = acceptedHelper;
            }
        }
        if (showAuthorContact) {
            m.put("contactType", r.getContactType());
            m.put("contactValue", r.getContactValue());
        } else {
            m.put("contactType", null);
            m.put("contactValue", null);
        }
        return m;
    }

    private Map<String, Object> toReplyMap(HelpReply rep, HelpRequest req, Long viewerUserId, Map<Long, User> users) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", rep.getId());
        m.put("requestId", rep.getRequestId());
        m.put("userId", rep.getUserId());
        m.put("message", rep.getMessage());
        m.put("status", rep.getStatus());
        m.put("statusLabel", replyStatusLabelCn(rep.getStatus()));
        m.put("isHelper", Boolean.TRUE.equals(rep.getIsHelper()));
        m.put("contactWilling", Boolean.TRUE.equals(rep.getContactWilling()));
        m.put("createdAt", rep.getCreatedAt());
        User u = users.get(rep.getUserId());
        m.put("userName", u != null ? displayName(u) : "用户");

        boolean showReplyContact = false;
        if (viewerUserId != null) {
            if (viewerUserId.equals(rep.getUserId())) {
                showReplyContact = true;
            } else if (viewerUserId.equals(req.getUserId()) && "accepted".equals(rep.getStatus())) {
                showReplyContact = true;
            }
        }
        if (showReplyContact && Boolean.TRUE.equals(rep.getContactWilling())) {
            m.put("contactType", rep.getContactType());
            m.put("contactValue", rep.getContactValue());
        } else {
            m.put("contactType", null);
            m.put("contactValue", null);
        }
        return m;
    }

    private Map<Long, User> resolveUserMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }
        List<Long> distinct = userIds.stream().filter(Objects::nonNull).distinct().toList();
        Map<Long, User> map = new HashMap<>();
        for (User u : userRepository.findAllById(distinct)) {
            map.put(u.getUserid(), u);
        }
        return map;
    }

    private static String displayName(User u) {
        if (u.getUsername() != null && !u.getUsername().isBlank()) {
            return u.getUsername();
        }
        return "用户" + u.getUserid();
    }

    private static String normalizeHelpType(String raw) {
        String t = raw == null ? "" : raw.trim().toUpperCase();
        if (!ALLOWED_HELP_TYPES.contains(t)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择有效的需求类型");
        }
        return t;
    }

    private static String normalizeListTypeFilter(String raw) {
        if (raw == null || raw.isBlank() || "ALL".equalsIgnoreCase(raw.trim())) {
            return null;
        }
        String t = raw.trim().toUpperCase();
        if (!ALLOWED_HELP_TYPES.contains(t)) {
            return null;
        }
        return t;
    }

    private static java.time.LocalDate parseDeadline(Object v) {
        if (v == null) {
            return null;
        }
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(s);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "截止日期格式无效");
        }
    }

    private void bumpHelpCount(long userId) {
        UserHelpStats s = userHelpStatsRepository.findById(userId).orElseGet(() -> {
            UserHelpStats x = new UserHelpStats();
            x.setUserId(userId);
            return x;
        });
        s.setHelpCount((s.getHelpCount() == null ? 0 : s.getHelpCount()) + 1);
        refreshCreditScore(s);
        userHelpStatsRepository.save(s);
    }

    private void bumpAcceptedCount(long userId) {
        UserHelpStats s = userHelpStatsRepository.findById(userId).orElseGet(() -> {
            UserHelpStats x = new UserHelpStats();
            x.setUserId(userId);
            return x;
        });
        s.setAcceptedCount((s.getAcceptedCount() == null ? 0 : s.getAcceptedCount()) + 1);
        refreshCreditScore(s);
        userHelpStatsRepository.save(s);
    }

    private void bumpSuccessCount(long userId) {
        UserHelpStats s = userHelpStatsRepository.findById(userId).orElseGet(() -> {
            UserHelpStats x = new UserHelpStats();
            x.setUserId(userId);
            return x;
        });
        s.setSuccessCount((s.getSuccessCount() == null ? 0 : s.getSuccessCount()) + 1);
        refreshCreditScore(s);
        userHelpStatsRepository.save(s);
    }

    private void refreshCreditScore(UserHelpStats s) {
        int help = s.getHelpCount() == null ? 0 : s.getHelpCount();
        int accepted = s.getAcceptedCount() == null ? 0 : s.getAcceptedCount();
        int success = s.getSuccessCount() == null ? 0 : s.getSuccessCount();
        s.setCreditScore(help + accepted * 3 + success * 10);
    }

    public Map<String, Object> getLeaderboard(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        List<UserHelpStats> stats = userHelpStatsRepository.findTop20ByOrderByCreditScoreDescSuccessCountDesc()
                .stream().limit(safeLimit).toList();
        List<Long> userIds = stats.stream().map(UserHelpStats::getUserId).toList();
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));

        List<Map<String, Object>> items = new ArrayList<>();
        int rank = 1;
        for (UserHelpStats s : stats) {
            if (safe(s.getCreditScore()) <= 0) continue;
            User u = userMap.get(s.getUserId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("rank", rank++);
            row.put("userId", s.getUserId());
            row.put("nickname", u != null ? displayName(u) : "用户");
            row.put("creditScore", s.getCreditScore());
            row.put("successCount", s.getSuccessCount());
            row.put("helpCount", s.getHelpCount());
            items.add(row);
        }
        return Map.of("items", items);
    }

    private int safe(Integer v) {
        return v == null ? 0 : v;
    }
}
