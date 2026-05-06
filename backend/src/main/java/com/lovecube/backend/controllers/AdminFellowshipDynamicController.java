package com.lovecube.backend.controllers;

import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.AdminFellowshipDynamicService;
import com.lovecube.backend.services.PermissionConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端：联谊动态（{@code dynamics} 表）列表与点赞明细。
 * <p>
 * 外部完整路径：{@code {context-path}/api/admin/fellowship-dynamics}（本地常见为 {@code /admin/api/admin/fellowship-dynamics}）。
 * 权限：{@link PermissionConstants#REVIEW_MANAGE}。
 * <p>
 * 响应为 {@code Map}，分页字段统一为 {@code total}、{@code pageNum}、{@code pageSize}、{@code hasNext}。
 * 列表体同时提供 {@code rows} 与 {@code list}（内容相同，{@code list} 为兼容字段）。
 * <p>
 * 动态行（联谊动态）推荐字段：{@code fellowshipDynamicId}、{@code authorId}、{@code authorName}、{@code authorAvatar}、
 * {@code content}、{@code images}、{@code likeCount}、{@code commentCount}、{@code publishedAt}；
 * 兼容旧字段：{@code id}、{@code userId}、{@code username}、{@code userAvatar}、{@code createdAt}。
 * <p>
 * 点赞行推荐字段：{@code fellowshipDynamicId}、{@code likedUserId}、{@code likedUsername}、{@code likedUserAvatar}、{@code likedAt}；
 * 兼容旧字段：{@code userId}、{@code username}、{@code userAvatar}、{@code createdAt}。
 */
@RestController
@RequestMapping("/api/admin/fellowship-dynamics")
public class AdminFellowshipDynamicController {

    private final AdminFellowshipDynamicService adminFellowshipDynamicService;
    private final AdminAuthService adminAuthService;

    public AdminFellowshipDynamicController(
            AdminFellowshipDynamicService adminFellowshipDynamicService,
            AdminAuthService adminAuthService) {
        this.adminFellowshipDynamicService = adminFellowshipDynamicService;
        this.adminAuthService = adminAuthService;
    }

    /**
     * 分页列出未删除的联谊动态（按创建时间倒序）。
     *
     * @see AdminFellowshipDynamicController 类注释中的响应字段说明
     */
    @GetMapping
    public Map<String, Object> list(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.REVIEW_MANAGE);
        return adminFellowshipDynamicService.listDynamics(pageNum, pageSize);
    }

    /**
     * 分页列出某条联谊动态的点赞记录（按点赞时间倒序）。
     *
     * @param id 联谊动态主键（{@code dynamics.id}）
     * @see AdminFellowshipDynamicController 类注释中的响应字段说明
     */
    @GetMapping("/{id}/likes")
    public Map<String, Object> listLikes(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "50") int pageSize) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.REVIEW_MANAGE);
        return adminFellowshipDynamicService.listLikesForDynamic(id, pageNum, pageSize);
    }
}
