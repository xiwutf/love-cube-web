package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.notification.NotificationCatalog;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.GrowthService;
import com.lovecube.backend.services.NotificationService;
import com.lovecube.backend.services.UnifiedProfileService;
import com.lovecube.backend.services.UserInteractionService;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interactions")
public class InteractionController {
    
    @Autowired
    private UserInteractionService interactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private GrowthService growthService;

    @Autowired
    private UnifiedProfileService unifiedProfileService;

    private ResponseEntity<?> forbiddenIfNeedsFellowshipPhotos(User u) {
        if (u != null && unifiedProfileService.isFellowshipActiveButMissingLifePhotos(u)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(unifiedProfileService.buildFellowshipPhotosRequiredErrorBody());
        }
        return null;
    }

    /**
     * 点赞用户
     */
    @PostMapping("/like/{userId}")
    public ResponseEntity<?> likeUser(@PathVariable Long userId,
                                     @RequestHeader("Authorization") String authHeader) {
        try {
            User currentUser = getCurrentUser(authHeader);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "用户认证失败"));
            }
            
            // 检查是否已经点赞
            boolean isLiked = interactionService.isLiked(currentUser.getUserid(), userId, userId);
            if (isLiked) {
                // 如果已经点赞，则取消点赞
                interactionService.unlikeUser(currentUser.getUserid(), userId);
                return ResponseEntity.ok(Map.of(
                    "message", "取消点赞成功",
                    "isLiked", false
                ));
            } else {
                ResponseEntity<?> need = forbiddenIfNeedsFellowshipPhotos(currentUser);
                if (need != null) {
                    return need;
                }
                // 如果没有点赞，则点赞
                interactionService.likeUser(currentUser.getUserid(), userId);
                growthService.recordAction(currentUser.getUserid(), "LIKE_CONTENT", "LIKE_USER_" + userId);
                boolean matched = interactionService.checkMutualLike(currentUser.getUserid(), userId);

                String senderName = currentUser.getUsername() != null ? currentUser.getUsername() : "有人";
                try {
                    notificationService.send(userId, NotificationCatalog.TYPE_PROFILE_LIKED, "有人喜欢你",
                        senderName + " 喜欢了你", "USER", String.valueOf(currentUser.getUserid()));
                    if (matched) {
                        notificationService.send(userId, NotificationCatalog.TYPE_MUTUAL_MATCH, "你们配对成功！",
                            "你和 " + senderName + " 互相喜欢，快去打招呼吧 🎉", "USER",
                            String.valueOf(currentUser.getUserid()));
                    }
                } catch (Exception ignored) {}

                java.util.Map<String, Object> likeResult = new java.util.HashMap<>();
                likeResult.put("message", "点赞成功");
                likeResult.put("isLiked", true);
                likeResult.put("matched", matched);
                likeResult.put("matchedUserId", matched ? userId : null);
                return ResponseEntity.ok(likeResult);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "操作失败: " + e.getMessage()));
        }
    }
    
    /**
     * 关注用户
     */
    @PostMapping("/follow/{userId}")
    public ResponseEntity<?> followUser(@PathVariable Long userId,
                                       @RequestHeader("Authorization") String authHeader) {
        try {
            User currentUser = getCurrentUser(authHeader);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "用户认证失败"));
            }
            
            // 检查是否已经关注
            boolean isFollowing = interactionService.isFollowing(currentUser.getUserid(), userId);
            if (isFollowing) {
                // 如果已经关注，则取消关注
                interactionService.unfollowUser(currentUser.getUserid(), userId);
                return ResponseEntity.ok(Map.of(
                    "message", "取消关注成功",
                    "isFollowing", false
                ));
            } else {
                ResponseEntity<?> need = forbiddenIfNeedsFellowshipPhotos(currentUser);
                if (need != null) {
                    return need;
                }
                // 如果没有关注，则关注
                interactionService.followUser(currentUser.getUserid(), userId);
                String senderName = currentUser.getUsername() != null ? currentUser.getUsername() : "有人";
                try {
                    notificationService.createNotification(
                            userId,
                            NotificationCatalog.TYPE_USER_FOLLOWED,
                            senderName + " 关注了你",
                            senderName + " 关注了你",
                            "/fellowship/user-profile/" + currentUser.getUserid(),
                            "USER",
                            String.valueOf(currentUser.getUserid()));
                } catch (Exception ignored) {}
                return ResponseEntity.ok(Map.of(
                    "message", "关注成功",
                    "isFollowing", true
                ));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "操作失败: " + e.getMessage()));
        }
    }

    /**
     * 认识页收藏（关注）：只增加关注、不切换取消；类型为 FOLLOW，与「超级喜欢」SUPER_LIKE 区分。
     */
    @PostMapping("/match-follow/{userId}")
    public ResponseEntity<?> matchFollowUser(@PathVariable Long userId,
                                             @RequestHeader("Authorization") String authHeader) {
        try {
            User currentUser = getCurrentUser(authHeader);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "用户认证失败"));
            }
            if (!interactionService.isFollowing(currentUser.getUserid(), userId)) {
                ResponseEntity<?> need = forbiddenIfNeedsFellowshipPhotos(currentUser);
                if (need != null) {
                    return need;
                }
            }
            boolean created = interactionService.followUserIfNotFollowing(currentUser.getUserid(), userId);
            boolean matched = interactionService.checkMutualLike(currentUser.getUserid(), userId);
            String senderName = currentUser.getUsername() != null ? currentUser.getUsername() : "有人";
            if (created) {
                try {
                    notificationService.createNotification(
                            userId,
                            NotificationCatalog.TYPE_USER_FOLLOWED,
                            senderName + " 关注了你",
                            senderName + " 关注了你",
                            "/fellowship/user-profile/" + currentUser.getUserid(),
                            "USER",
                            String.valueOf(currentUser.getUserid()));
                    if (matched) {
                        notificationService.send(userId, NotificationCatalog.TYPE_MUTUAL_MATCH, "你们配对成功！",
                                "你和 " + senderName + " 互相喜欢，快去打招呼吧 🎉", "USER",
                                String.valueOf(currentUser.getUserid()));
                    }
                } catch (Exception ignored) {
                }
            }
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("message", created ? "收藏成功" : "已收藏");
            result.put("isFollowing", true);
            result.put("matched", matched);
            result.put("matchedUserId", matched ? userId : null);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "操作失败: " + e.getMessage()));
        }
    }

    /**
     * 跳过用户（不感兴趣）
     */
    @PostMapping("/dislike/{userId}")
    public ResponseEntity<?> dislikeUser(@PathVariable Long userId,
                                         @RequestHeader("Authorization") String authHeader) {
        try {
            User currentUser = getCurrentUser(authHeader);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "用户认证失败"));
            }
            interactionService.skipUser(currentUser.getUserid(), userId);
            return ResponseEntity.ok(Map.of("message", "已跳过", "skipped", true, "matched", false));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "操作失败: " + e.getMessage()));
        }
    }

    /**
     * 超级喜欢用户
     */
    @PostMapping("/superlike/{userId}")
    public ResponseEntity<?> superlikeUser(@PathVariable Long userId,
                                           @RequestHeader("Authorization") String authHeader) {
        try {
            User currentUser = getCurrentUser(authHeader);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "用户认证失败"));
            }
            ResponseEntity<?> need = forbiddenIfNeedsFellowshipPhotos(currentUser);
            if (need != null) {
                return need;
            }
            interactionService.superLikeUser(currentUser.getUserid(), userId);
            boolean matched = interactionService.checkMutualLike(currentUser.getUserid(), userId);
            String senderName = currentUser.getUsername() != null ? currentUser.getUsername() : "有人";
            try {
                notificationService.send(userId, NotificationCatalog.TYPE_PROFILE_LIKED, "有人超级喜欢你",
                        senderName + " 超级喜欢了你", "USER", String.valueOf(currentUser.getUserid()));
                if (matched) {
                    notificationService.send(userId, NotificationCatalog.TYPE_MUTUAL_MATCH, "你们配对成功！",
                            "你和 " + senderName + " 互相喜欢，快去打招呼吧 🎉", "USER",
                            String.valueOf(currentUser.getUserid()));
                }
            } catch (Exception ignored) {}
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("message", "超级喜欢成功");
            result.put("isLiked", true);
            result.put("matched", matched);
            result.put("matchedUserId", matched ? userId : null);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "操作失败: " + e.getMessage()));
        }
    }

    /**
     * 查询当前用户是否已喜欢某用户
     */
    @GetMapping("/like-status/{userId}")
    public ResponseEntity<?> getLikeStatus(@PathVariable Long userId,
                                            @RequestHeader("Authorization") String authHeader) {
        try {
            User currentUser = getCurrentUser(authHeader);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "用户认证失败"));
            }
            boolean isLiked = interactionService.isLiked(currentUser.getUserid(), userId, userId);
            return ResponseEntity.ok(Map.of("isLiked", isLiked));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "查询失败"));
        }
    }

    /**
     * 我点赞的人
     */
    @GetMapping("/likes/sent")
    public ResponseEntity<?> getMyLikes(@RequestHeader("Authorization") String authHeader) {
        User currentUser = getCurrentUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户认证失败"));
        }
        List<Map<String, Object>> list = interactionService.getSentLikeUsers(currentUser.getUserid());
        return ResponseEntity.ok(list);
    }

    /**
     * 互相喜欢
     */
    @GetMapping("/likes/mutual")
    public ResponseEntity<?> getMutualLikes(@RequestHeader("Authorization") String authHeader) {
        User currentUser = getCurrentUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户认证失败"));
        }
        List<Map<String, Object>> list = interactionService.getMutualLikeUsers(currentUser.getUserid());
        return ResponseEntity.ok(list);
    }

    /**
     * 认识页：分页查看自己划过的人（喜欢 / 超级喜欢、跳过、或全部时间线）
     *
     * @param tab liked（默认）| skipped | all
     */
    @GetMapping("/match-history")
    public ResponseEntity<?> getMatchBrowseHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "liked") String tab,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            User currentUser = getCurrentUser(authHeader);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户认证失败"));
            }
            int safePage = page == null || page < 1 ? 1 : page;
            int safeSize = size == null ? 20 : Math.min(Math.max(size, 1), 50);
            Map<String, Object> payload = interactionService.getMatchBrowseHistoryPage(
                    currentUser.getUserid(), tab, safePage, safeSize);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", payload.get("list"));
            response.put("page", payload.get("page"));
            response.put("size", payload.get("size"));
            response.put("total", payload.get("total"));
            response.put("hasMore", payload.get("hasMore"));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "加载失败"));
        }
    }

    /**
     * 喜欢我的人
     */
    @GetMapping("/likes/received")
    public ResponseEntity<?> getReceivedLikes(@RequestHeader("Authorization") String authHeader) {
        User currentUser = getCurrentUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "鐢ㄦ埛璁よ瘉澶辫触"));
        }
        List<Map<String, Object>> list = interactionService.getReceivedLikeUsers(currentUser.getUserid());
        return ResponseEntity.ok(list);
    }

    /**
     * 我的关注
     */
    @GetMapping("/following")
    public ResponseEntity<?> getFollowing(@RequestHeader("Authorization") String authHeader) {
        User currentUser = getCurrentUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户认证失败"));
        }
        List<Map<String, Object>> list = interactionService.getFollowingUsers(currentUser.getUserid());
        return ResponseEntity.ok(list);
    }
    
    /**
     * 从token获取当前用户
     */
    private User getCurrentUser(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }
            
            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);
            
            if (openid == null) {
                return null;
            }
            
            return userRepository.findByOpenid(openid);
        } catch (Exception e) {
            return null;
        }
    }
}
