package com.lovecube.backend.services;

import com.lovecube.backend.entity.UserNotificationSetting;
import com.lovecube.backend.notification.NotificationCatalog;
import com.lovecube.backend.repository.UserNotificationSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationSettingService {

    private final UserNotificationSettingRepository settingRepository;

    public NotificationSettingService(UserNotificationSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    /**
     * 为新用户预置各类型的默认开关（幂等），在打开消息列表/未读数等入口时也可调用。
     */
    @Transactional
    public void ensureDefaultRows(Long userId) {
        if (userId == null) return;
        ensureDefaults(userId);
    }

    @Transactional
    public List<Map<String, Object>> getOrCreateSettings(Long userId) {
        ensureDefaults(userId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (UserNotificationSetting row : settingRepository.findByUserId(userId)) {
            out.add(toMap(row));
        }
        return out;
    }

    /**
     * 解绑微信公众号后，关闭该用户所有类型的「微信通知」开关，避免误以为仍可推送。
     */
    @Transactional
    public void disableAllWechatPreferences(Long userId) {
        if (userId == null) return;
        settingRepository.disableAllWechatForUser(userId);
    }

    @Transactional
    public void updateSettings(Long userId, List<Map<String, Object>> items) {
        if (items == null) return;
        for (Map<String, Object> raw : items) {
            if (raw == null) continue;
            String type = String.valueOf(raw.getOrDefault("type", "")).trim();
            if (type.isEmpty()) continue;
            UserNotificationSetting row = settingRepository.findByUserIdAndType(userId, type)
                    .orElseGet(() -> {
                        UserNotificationSetting n = new UserNotificationSetting();
                        n.setUserId(userId);
                        n.setType(type);
                        n.setSiteEnabled(NotificationCatalog.defaultSiteEnabled(type));
                        n.setWechatEnabled(NotificationCatalog.defaultWechatEnabled(type));
                        return n;
                    });
            if (raw.containsKey("siteEnabled")) {
                row.setSiteEnabled(Boolean.parseBoolean(String.valueOf(raw.get("siteEnabled"))));
            }
            if (raw.containsKey("wechatEnabled")) {
                row.setWechatEnabled(Boolean.parseBoolean(String.valueOf(raw.get("wechatEnabled"))));
            }
            settingRepository.save(row);
        }
    }

    private void ensureDefaults(Long userId) {
        for (String t : NotificationCatalog.allConfigurableTypes()) {
            if (settingRepository.findByUserIdAndType(userId, t).isEmpty()) {
                UserNotificationSetting s = new UserNotificationSetting();
                s.setUserId(userId);
                s.setType(t);
                s.setSiteEnabled(NotificationCatalog.defaultSiteEnabled(t));
                s.setWechatEnabled(NotificationCatalog.defaultWechatEnabled(t));
                settingRepository.save(s);
            }
        }
    }

    private Map<String, Object> toMap(UserNotificationSetting row) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", row.getId());
        m.put("type", row.getType());
        m.put("siteEnabled", Boolean.TRUE.equals(row.getSiteEnabled()));
        m.put("wechatEnabled", Boolean.TRUE.equals(row.getWechatEnabled()));
        return m;
    }
}
