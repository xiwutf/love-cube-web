package com.lovecube.backend.services;

import com.lovecube.backend.entity.UserNotification;
import com.lovecube.backend.entity.UserNotificationSetting;
import com.lovecube.backend.entity.UserSocialBinding;
import com.lovecube.backend.notification.NotificationCatalog;
import com.lovecube.backend.repository.UserNotificationSettingRepository;
import com.lovecube.backend.repository.UserSocialBindingRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 通知系统统一调度：根据用户设置与绑定关系计算微信占位 {@code push_status}（不接真实微信 API）。
 * <p><strong>禁止绕过本类</strong>在业务代码中自行决定或写死推送状态；与 {@link NotificationService} 配合：
 * 本类仅做内存侧计算，对 {@code user_notifications} 的写入统一由 {@link NotificationService} 执行。</p>
 */
@Service
public class NotificationDispatchService {

    private final UserNotificationSettingRepository settingRepository;
    private final UserSocialBindingRepository socialBindingRepository;

    public NotificationDispatchService(
            UserNotificationSettingRepository settingRepository,
            UserSocialBindingRepository socialBindingRepository
    ) {
        this.settingRepository = settingRepository;
        this.socialBindingRepository = socialBindingRepository;
    }

    /**
     * 仅修改内存中的 {@link UserNotification}（推送状态等），不访问数据库。
     * 调用方须在持久化前调用；{@code save} 仅允许发生在 {@link NotificationService} 内。
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
}
