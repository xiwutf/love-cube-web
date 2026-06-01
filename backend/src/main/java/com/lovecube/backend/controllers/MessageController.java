package com.lovecube.backend.controllers;

import com.lovecube.backend.models.ChatMessage;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.ChatMessageRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.UserInteractionService;
import com.lovecube.backend.services.UserVisitorService;
import com.lovecube.backend.services.VipService;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserInteractionService interactionService;
    
    @Autowired
    private UserVisitorService visitorService;

    @Autowired
    private VipService vipService;

    /**
     * 获取聊天列表
     */
    @GetMapping("/messages/chat")
    public ResponseEntity<?> getChatList(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "未提供或格式错误的 token"));
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "token 无效"));
            }

            // 获取当前用户
            User currentUser = userRepository.findByOpenid(openid);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "用户不存在"));
            }

            Long userId = currentUser.getUserid();

            // 已有聊天记录的会话
            List<Object[]> chatPartners = chatMessageRepository.findDistinctChatPartners(userId);
            Set<Long> partnerIds = new HashSet<>();
            List<Map<String, Object>> chatList = new ArrayList<>();

            for (Object[] obj : chatPartners) {
                Long partnerId = ((Number) obj[0]).longValue();
                String username = (String) obj[1];
                String avatar = (String) obj[2];
                ChatMessage latestMessage = chatMessageRepository.findLatestChatMessage(userId, partnerId);

                Map<String, Object> chatItem = new HashMap<>();
                chatItem.put("id", partnerId);
                chatItem.put("userId", partnerId);
                chatItem.put("partnerId", partnerId);
                chatItem.put("nickname", username != null ? username : "未知用户");
                chatItem.put("avatarUrl", avatar != null ? avatar : "/images/default-avatar.png");
                chatItem.put("lastMessage", latestMessage != null ? latestMessage.getContent() : "暂无消息");
                chatItem.put("lastTime", latestMessage != null ? latestMessage.getTimestamp() : System.currentTimeMillis());
                chatItem.put("unread", getUnreadMessageCount(userId, partnerId));
                chatItem.put("matchedOnly", false);
                chatList.add(chatItem);
                partnerIds.add(partnerId);
            }

            // 互赞但尚未发消息的配对，补进列表便于首聊
            for (Map<String, Object> mutual : interactionService.getMutualLikeUsers(userId)) {
                Object uidObj = mutual.get("userId");
                if (uidObj == null) continue;
                Long partnerId = ((Number) uidObj).longValue();
                if (partnerIds.contains(partnerId)) continue;

                Map<String, Object> chatItem = new HashMap<>();
                chatItem.put("id", partnerId);
                chatItem.put("userId", partnerId);
                chatItem.put("partnerId", partnerId);
                chatItem.put("nickname", mutual.get("nickname") != null ? mutual.get("nickname") : "未知用户");
                Object avatarUrl = mutual.get("avatarUrl");
                chatItem.put("avatarUrl", avatarUrl != null ? avatarUrl : "/images/default-avatar.png");
                chatItem.put("lastMessage", "配对成功，打个招呼吧");
                chatItem.put("lastTime", toEpochMillis(mutual.get("createdAt")));
                chatItem.put("unread", 0);
                chatItem.put("matchedOnly", true);
                chatList.add(chatItem);
                partnerIds.add(partnerId);
            }

            chatList.sort((a, b) -> Long.compare((Long) b.get("lastTime"), (Long) a.get("lastTime")));

            return ResponseEntity.ok(chatList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "获取聊天列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取互动列表（点赞、评论等）
     */
    @GetMapping("/messages/interact")
    public ResponseEntity<?> getInteractList(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "未提供或格式错误的 token"));
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "token 无效"));
            }

            // 获取当前用户
            User currentUser = userRepository.findByOpenid(openid);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "用户不存在"));
            }

            Long userId = currentUser.getUserid();
            
            // 获取互动列表（默认获取前20条）
            List<Map<String, Object>> interactList = interactionService.getInteractionList(userId, 0, 20);

            return ResponseEntity.ok(interactList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "获取互动列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取访客列表
     */
    @GetMapping("/messages/visitor")
    public ResponseEntity<?> getVisitorList(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "未提供或格式错误的 token"));
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "token 无效"));
            }

            // 获取当前用户
            User currentUser = userRepository.findByOpenid(openid);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "用户不存在"));
            }

            Long userId = currentUser.getUserid();
            
            List<Map<String, Object>> visitorList = visitorService.getVisitorList(userId, 0, 20);
            boolean vipActive = vipService.isActiveVip(currentUser);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("vipActive", vipActive);
            body.put("totalCount", visitorList.size());
            if (vipActive) {
                body.put("items", visitorList);
            } else {
                body.put("items", visitorList.stream().map(this::maskVisitor).collect(Collectors.toList()));
                body.put("locked", true);
                body.put("upgradeHint", "开通 VIP 可查看完整访客信息");
            }
            return ResponseEntity.ok(body);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "获取访客列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取未读消息数量统计
     */
    @GetMapping("/messages/unread")
    public ResponseEntity<?> getUnreadCount(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "未提供或格式错误的 token"));
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "token 无效"));
            }

            // 获取当前用户
            User currentUser = userRepository.findByOpenid(openid);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "用户不存在"));
            }

            Long userId = currentUser.getUserid();

            // 获取未读消息数量
            Long chatUnread = chatMessageRepository.countUnreadMessages(userId);
            
            // 获取未读互动数量
            Long interactUnread = interactionService.getUnreadInteractionCount(userId);
            
            // 获取今日新访客数量（作为访客未读数量）
            Long visitorUnread = visitorService.getTodayVisitorCount(userId);
            
            Map<String, Object> unreadCount = new HashMap<>();
            unreadCount.put("chat", chatUnread != null ? chatUnread.intValue() : 0);
            unreadCount.put("interact", interactUnread != null ? interactUnread.intValue() : 0);
            unreadCount.put("visitor", visitorUnread != null ? visitorUnread.intValue() : 0);

            return ResponseEntity.ok(unreadCount);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "获取未读消息数量失败: " + e.getMessage()));
        }
    }

    /**
     * 获取两个用户之间的未读消息数量
     */
    private int getUnreadMessageCount(Long userId, Long partnerId) {
        try {
            List<ChatMessage> unreadMessages = chatMessageRepository.findByReceiverIdAndIsReadFalse(userId);
            return (int) unreadMessages.stream()
                .filter(msg -> msg.getSenderId().equals(partnerId))
                .count();
        } catch (Exception e) {
            return 0;
        }
    }

    private long toEpochMillis(Object createdAt) {
        if (createdAt instanceof LocalDateTime ldt) {
            return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        if (createdAt instanceof java.util.Date date) {
            return date.getTime();
        }
        return System.currentTimeMillis();
    }

    /**
     * 标记互动消息为已读
     */
    @PutMapping("/messages/interact/markRead")
    public ResponseEntity<?> markInteractAsRead(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "未提供或格式错误的 token"));
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "token 无效"));
            }

            // 获取当前用户
            User currentUser = userRepository.findByOpenid(openid);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "用户不存在"));
            }

            Long userId = currentUser.getUserid();
            
            // 标记所有互动消息为已读
            interactionService.markAllInteractionsAsRead(userId);

            return ResponseEntity.ok(Map.of("message", "互动消息已标记为已读"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "标记互动消息已读失败: " + e.getMessage()));
        }
    }

    /**
     * 标记访客消息为已读
     */
    @PutMapping("/messages/visitor/markRead")
    public ResponseEntity<?> markVisitorAsRead(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "未提供或格式错误的 token"));
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "token 无效"));
            }

            // 获取当前用户
            User currentUser = userRepository.findByOpenid(openid);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "用户不存在"));
            }

            Long userId = currentUser.getUserid();
            
            // 标记所有访客记录为已读
            visitorService.markAllVisitorsAsRead(userId);

            return ResponseEntity.ok(Map.of("message", "访客消息已标记为已读"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "标记访客消息已读失败: " + e.getMessage()));
        }
    }

    private Map<String, Object> maskVisitor(Map<String, Object> row) {
        Map<String, Object> masked = new LinkedHashMap<>(row);
        masked.put("nickname", "神秘访客");
        masked.put("avatarUrl", "/images/default-avatar.png");
        masked.put("locked", true);
        masked.remove("visitorId");
        masked.remove("userId");
        Map<String, Object> visitor = new LinkedHashMap<>();
        visitor.put("nickname", "神秘访客");
        visitor.put("avatarUrl", "/images/default-avatar.png");
        masked.put("visitor", visitor);
        return masked;
    }
} 