package com.lovecube.backend.services;

import com.lovecube.backend.entity.NotificationDispatchRecord;
import com.lovecube.backend.entity.UserNotification;
import com.lovecube.backend.entity.UserNotificationChannelPref;
import com.lovecube.backend.entity.UserNotificationSetting;
import com.lovecube.backend.entity.UserSocialBinding;
import com.lovecube.backend.models.User;
import com.lovecube.backend.notification.NotificationCatalog;
import com.lovecube.backend.repository.NotificationDispatchRecordRepository;
import com.lovecube.backend.repository.UserNotificationChannelPrefRepository;
import com.lovecube.backend.repository.UserNotificationRepository;
import com.lovecube.backend.repository.UserNotificationSettingRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.repository.UserSocialBindingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 通知统一调度：站内消息创建后异步投递邮件与 PushPlus；微信占位 {@code push_status} 仍在内存中计算。
 */
@Service
public class NotificationDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    public static final String CHANNEL_EMAIL = "EMAIL";
    public static final String CHANNEL_PUSHPLUS = "PUSHPLUS";

    public static final String DISPATCH_PENDING = "PENDING";
    public static final String DISPATCH_SENT = "SENT";
    public static final String DISPATCH_FAILED = "FAILED";
    public static final String DISPATCH_SKIPPED = "SKIPPED";

    private final UserNotificationSettingRepository settingRepository;
    private final UserSocialBindingRepository socialBindingRepository;
    private final UserNotificationChannelPrefRepository channelPrefRepository;
    private final NotificationDispatchRecordRepository dispatchRecordRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${lovecube.notification.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${lovecube.notification.mail.from-name:Love Cube}")
    private String mailFromName;

    @Value("${lovecube.notification.pushplus.url:https://www.pushplus.plus/send}")
    private String pushplusUrl;

    public NotificationDispatchService(
            UserNotificationSettingRepository settingRepository,
            UserSocialBindingRepository socialBindingRepository,
            UserNotificationChannelPrefRepository channelPrefRepository,
            NotificationDispatchRecordRepository dispatchRecordRepository,
            UserNotificationRepository userNotificationRepository,
            UserRepository userRepository,
            @org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender
    ) {
        this.settingRepository = settingRepository;
        this.socialBindingRepository = socialBindingRepository;
        this.channelPrefRepository = channelPrefRepository;
        this.dispatchRecordRepository = dispatchRecordRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    /**
     * 仅修改内存中的 {@link UserNotification}（微信占位推送状态），不访问数据库。
     */
    public void applyPushInMemory(UserNotification notification) {
        if (notification == null) {
            return;
        }
        String type = notification.getType();
        if (NotificationCatalog.forceSkipWechat(type)) {
            notification.setPushStatus(NotificationCatalog.PUSH_SKIPPED);
            return;
        }
        Optional<UserNotificationSetting> row = settingRepository.findByUserIdAndType(notification.getUserId(), type);
        boolean wechatPref = row.map(UserNotificationSetting::getWechatEnabled)
            .orElseGet(() -> NotificationCatalog.defaultWechatEnabled(type));
        if (!wechatPref) {
            notification.setPushStatus(NotificationCatalog.PUSH_SKIPPED);
            return;
        }
        boolean hasWechat = socialBindingRepository.findByUserIdAndProvider(
                notification.getUserId(), UserSocialBinding.PROVIDER_WECHAT_OFFICIAL
        ).filter(b -> "BOUND".equalsIgnoreCase(b.getBindStatus())).isPresent();
        if (!hasWechat) {
            notification.setPushStatus(NotificationCatalog.PUSH_SKIPPED);
            return;
        }
        notification.setPushStatus(NotificationCatalog.PUSH_MOCK_SENT);
    }

    /**
     * 站内消息落库后触发外部渠道异步投递（邮件 / PushPlus）。
     */
    @Async("notificationDispatchExecutor")
    public void scheduleExternalDispatch(Long notificationId) {
        if (notificationId == null) {
            return;
        }
        UserNotification notification = userNotificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            return;
        }
        if (NotificationCatalog.forceSkipExternalChannels(notification.getType())) {
            return;
        }
        UserNotificationChannelPref pref = channelPrefRepository.findByUserId(notification.getUserId()).orElse(null);
        User user = userRepository.findById(notification.getUserId()).orElse(null);

        dispatchEmail(notification, pref, user);
        dispatchPushPlus(notification, pref);
    }

    private void dispatchEmail(UserNotification notification, UserNotificationChannelPref pref, User user) {
        if (pref == null || !Boolean.TRUE.equals(pref.getEmailEnabled())) {
            saveSkipped(notification, CHANNEL_EMAIL, "邮件通知未开启");
            return;
        }
        String email = user != null ? user.getEmail() : null;
        if (email == null || email.isBlank()) {
            saveSkipped(notification, CHANNEL_EMAIL, "账号未绑定邮箱");
            return;
        }
        if (!mailEnabled || mailSender == null) {
            saveSkipped(notification, CHANNEL_EMAIL, "邮件服务未配置");
            return;
        }
        NotificationDispatchRecord record = createPending(notification, CHANNEL_EMAIL);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (mailFrom != null && !mailFrom.isBlank()) {
                message.setFrom(mailFrom);
            }
            message.setTo(email);
            message.setSubject("【" + mailFromName + "】" + notification.getTitle());
            String body = notification.getContent();
            if (notification.getLinkUrl() != null && !notification.getLinkUrl().isBlank()) {
                body = body + "\n\n查看详情：" + notification.getLinkUrl();
            }
            message.setText(body);
            mailSender.send(message);
            markSent(record);
        } catch (Exception e) {
            log.warn("邮件通知发送失败 notificationId={} userId={}: {}", notification.getId(), notification.getUserId(), e.getMessage());
            markFailed(record, e.getMessage());
        }
    }

    private void dispatchPushPlus(UserNotification notification, UserNotificationChannelPref pref) {
        if (pref == null || !Boolean.TRUE.equals(pref.getPushplusEnabled())) {
            saveSkipped(notification, CHANNEL_PUSHPLUS, "PushPlus 未开启");
            return;
        }
        String token = pref.getPushplusToken();
        if (token == null || token.isBlank()) {
            saveSkipped(notification, CHANNEL_PUSHPLUS, "未配置 PushPlus Token");
            return;
        }
        NotificationDispatchRecord record = createPending(notification, CHANNEL_PUSHPLUS);
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("token", token);
            payload.put("title", notification.getTitle());
            String content = notification.getContent();
            if (notification.getLinkUrl() != null && !notification.getLinkUrl().isBlank()) {
                content = content + "\n" + notification.getLinkUrl();
            }
            payload.put("content", content);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    pushplusUrl,
                    new HttpEntity<>(payload, headers),
                    Map.class
            );
            Map<?, ?> body = resp.getBody();
            Object code = body != null ? body.get("code") : null;
            boolean ok = Integer.valueOf(200).equals(code) || "200".equals(String.valueOf(code));
            if (!ok) {
                String msg = "PushPlus 无响应体";
                if (body != null) {
                    Object rawMsg = body.get("msg");
                    msg = rawMsg != null ? String.valueOf(rawMsg) : "PushPlus 返回异常";
                }
                markFailed(record, msg);
                return;
            }
            markSent(record);
        } catch (Exception e) {
            log.warn("PushPlus 发送失败 notificationId={} userId={}: {}", notification.getId(), notification.getUserId(), e.getMessage());
            markFailed(record, e.getMessage());
        }
    }

    private NotificationDispatchRecord createPending(UserNotification notification, String channel) {
        NotificationDispatchRecord record = new NotificationDispatchRecord();
        record.setNotificationId(notification.getId());
        record.setUserId(notification.getUserId());
        record.setChannel(channel);
        record.setStatus(DISPATCH_PENDING);
        return dispatchRecordRepository.save(record);
    }

    private void saveSkipped(UserNotification notification, String channel, String reason) {
        NotificationDispatchRecord record = new NotificationDispatchRecord();
        record.setNotificationId(notification.getId());
        record.setUserId(notification.getUserId());
        record.setChannel(channel);
        record.setStatus(DISPATCH_SKIPPED);
        record.setErrorMessage(truncate(reason));
        dispatchRecordRepository.save(record);
    }

    private void markSent(NotificationDispatchRecord record) {
        record.setStatus(DISPATCH_SENT);
        record.setSentAt(LocalDateTime.now());
        record.setErrorMessage(null);
        dispatchRecordRepository.save(record);
    }

    private void markFailed(NotificationDispatchRecord record, String error) {
        record.setStatus(DISPATCH_FAILED);
        record.setErrorMessage(truncate(error));
        dispatchRecordRepository.save(record);
    }

    private static String truncate(String msg) {
        if (msg == null) return null;
        return msg.length() > 500 ? msg.substring(0, 500) : msg;
    }
}
