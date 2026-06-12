package com.lovecube.backend.services;

import com.lovecube.backend.entity.UserNotificationChannelPref;
import com.lovecube.backend.repository.UserNotificationChannelPrefRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserNotificationChannelPrefService {

    private final UserNotificationChannelPrefRepository prefRepository;
    private final UserRepository userRepository;

    public UserNotificationChannelPrefService(
            UserNotificationChannelPrefRepository prefRepository,
            UserRepository userRepository
    ) {
        this.prefRepository = prefRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserNotificationChannelPref getOrCreate(Long userId) {
        return prefRepository.findByUserId(userId).orElseGet(() -> {
            UserNotificationChannelPref pref = new UserNotificationChannelPref();
            pref.setUserId(userId);
            return prefRepository.save(pref);
        });
    }

    @Transactional
    public Map<String, Object> toApiMap(Long userId) {
        UserNotificationChannelPref pref = getOrCreate(userId);
        String email = userRepository.findById(userId).map(u -> u.getEmail()).orElse(null);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("emailEnabled", Boolean.TRUE.equals(pref.getEmailEnabled()));
        m.put("pushplusEnabled", Boolean.TRUE.equals(pref.getPushplusEnabled()));
        m.put("pushplusToken", pref.getPushplusToken() != null ? pref.getPushplusToken() : "");
        m.put("hasEmail", email != null && !email.isBlank());
        m.put("emailMasked", maskEmail(email));
        return m;
    }

    @Transactional
    public void update(Long userId, Map<String, Object> body) {
        UserNotificationChannelPref pref = getOrCreate(userId);
        if (body.containsKey("emailEnabled")) {
            pref.setEmailEnabled(Boolean.parseBoolean(String.valueOf(body.get("emailEnabled"))));
        }
        if (body.containsKey("pushplusEnabled")) {
            pref.setPushplusEnabled(Boolean.parseBoolean(String.valueOf(body.get("pushplusEnabled"))));
        }
        if (body.containsKey("pushplusToken")) {
            String token = String.valueOf(body.get("pushplusToken")).trim();
            pref.setPushplusToken(token.isEmpty() ? null : token);
        }
        if (Boolean.TRUE.equals(pref.getPushplusEnabled())
                && (pref.getPushplusToken() == null || pref.getPushplusToken().isBlank())) {
            throw new IllegalArgumentException("开启 PushPlus 前请填写 Token");
        }
        prefRepository.save(pref);
    }

    private static String maskEmail(String email) {
        if (email == null || email.isBlank()) return "";
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(Math.max(0, at));
        return email.charAt(0) + "***" + email.substring(at);
    }
}
