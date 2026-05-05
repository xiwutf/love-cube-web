package com.lovecube.backend.services;

import com.lovecube.backend.entity.UserSocialBinding;
import com.lovecube.backend.repository.UserSocialBindingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SocialBindingService {

    private final UserSocialBindingRepository bindingRepository;
    private final NotificationSettingService notificationSettingService;

    public SocialBindingService(UserSocialBindingRepository bindingRepository,
                                NotificationSettingService notificationSettingService) {
        this.bindingRepository = bindingRepository;
        this.notificationSettingService = notificationSettingService;
    }

    public List<Map<String, Object>> listBindings(Long userId) {
        return bindingRepository.findByUserId(userId).stream().map(this::toMap).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> mockBindWechatOfficial(Long userId) {
        UserSocialBinding row = bindingRepository.findByUserIdAndProvider(userId, UserSocialBinding.PROVIDER_WECHAT_OFFICIAL)
                .orElseGet(UserSocialBinding::new);
        row.setUserId(userId);
        row.setProvider(UserSocialBinding.PROVIDER_WECHAT_OFFICIAL);
        row.setOpenid("mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        row.setUnionid(null);
        row.setNickname("微信用户(模拟)");
        row.setAvatarUrl(null);
        row.setBindStatus("BOUND");
        UserSocialBinding saved = bindingRepository.save(row);
        notificationSettingService.ensureDefaultRows(userId);
        return toMap(saved);
    }

    @Transactional
    public boolean unbind(Long userId, Long bindingId) {
        Optional<UserSocialBinding> opt = bindingRepository.findById(bindingId);
        if (opt.isEmpty() || !userId.equals(opt.get().getUserId())) {
            return false;
        }
        String provider = opt.get().getProvider();
        int deleted = bindingRepository.deleteForUser(bindingId, userId);
        if (deleted > 0 && UserSocialBinding.PROVIDER_WECHAT_OFFICIAL.equals(provider)) {
            notificationSettingService.disableAllWechatPreferences(userId);
        }
        return deleted > 0;
    }

    private Map<String, Object> toMap(UserSocialBinding b) {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("provider", b.getProvider());
        String oid = b.getOpenid();
        m.put("openid", oid != null && oid.length() > 10 ? oid.substring(0, 6) + "****" + oid.substring(oid.length() - 4) : (oid != null ? oid : ""));
        m.put("nickname", b.getNickname() != null ? b.getNickname() : "");
        m.put("avatarUrl", b.getAvatarUrl() != null ? b.getAvatarUrl() : "");
        m.put("bindStatus", b.getBindStatus());
        m.put("createdAt", b.getCreatedAt());
        m.put("updatedAt", b.getUpdatedAt());
        return m;
    }
}
