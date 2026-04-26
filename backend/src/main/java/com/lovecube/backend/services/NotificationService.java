package com.lovecube.backend.services;

import com.lovecube.backend.entity.Notification;
import com.lovecube.backend.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void send(Long userId, String type, String title, String content,
                     String targetType, String targetId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setContent(content);
        n.setTargetType(targetType);
        n.setTargetId(targetId);
        notificationRepository.save(n);
    }

    public List<Notification> getMyNotifications(Long userId, int limit) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream().limit(Math.max(1, Math.min(limit, 200)))
            .collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    public void markRead(Long userId, Long notifId) {
        notificationRepository.findById(notifId).ifPresent(n -> {
            if (n.getUserId().equals(userId) && !Boolean.TRUE.equals(n.getIsRead())) {
                n.setIsRead(true);
                n.setReadAt(LocalDateTime.now());
                notificationRepository.save(n);
            }
        });
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllRead(userId, LocalDateTime.now());
    }
}
