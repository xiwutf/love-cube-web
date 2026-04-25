package com.lovecube.backend.services;

import com.lovecube.backend.entity.FellowshipProfile;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.FellowshipProfileRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FellowshipProfileService {
    private final FellowshipProfileRepository fellowshipProfileRepository;
    private final UserRepository userRepository;

    public FellowshipProfileService(
            FellowshipProfileRepository fellowshipProfileRepository,
            UserRepository userRepository
    ) {
        this.fellowshipProfileRepository = fellowshipProfileRepository;
        this.userRepository = userRepository;
    }

    public User requireCurrentUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("未登录");
        }
        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null || openid.isBlank()) {
            throw new IllegalArgumentException("登录凭证无效");
        }
        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    @Transactional
    public FellowshipProfile getMyProfile(User user) {
        return fellowshipProfileRepository.findByUserId(user.getUserid()).orElseGet(() -> {
            FellowshipProfile profile = new FellowshipProfile();
            profile.setUserId(user.getUserid());
            profile.setNickname(user.getUsername());
            profile.setAvatarUrl(user.getProfilePhoto());
            profile.setCity(user.getLocation());
            profile.setOccupation(user.getOccupation());
            profile.setBio(user.getBio());
            profile.setAge(user.getAge());
            profile.setBirthYear(user.getBirthDate() == null ? null : user.getBirthDate().getYear());
            profile.setGender(convertGender(user.getGender()));
            profile.setProfileStatus("INCOMPLETE");
            profile.setReviewStatus("PENDING");
            return fellowshipProfileRepository.save(profile);
        });
    }

    @Transactional
    public FellowshipProfile updateMyProfile(User user, Map<String, Object> payload) {
        FellowshipProfile profile = getMyProfile(user);

        profile.setNickname(getString(payload.get("nickname"), profile.getNickname()));
        profile.setGender(normalizeGender(getString(payload.get("gender"), profile.getGender())));
        profile.setBirthYear(getInteger(payload.get("birthYear"), profile.getBirthYear()));
        profile.setCity(getString(payload.get("city"), profile.getCity()));
        profile.setOccupation(getString(payload.get("occupation"), profile.getOccupation()));
        profile.setEducation(getString(payload.get("education"), profile.getEducation()));
        profile.setHeight(getInteger(payload.get("height"), profile.getHeight()));
        profile.setBio(getString(payload.get("bio"), profile.getBio()));
        profile.setIntention(getString(payload.get("intention"), profile.getIntention()));
        profile.setAvatarUrl(getString(payload.get("avatarUrl"), profile.getAvatarUrl()));
        profile.setTags(normalizeTags(payload.get("tags"), profile.getTags()));

        if (profile.getBirthYear() != null) {
            int year = LocalDate.now().getYear();
            if (profile.getBirthYear() > 1900 && profile.getBirthYear() <= year) {
                profile.setAge(year - profile.getBirthYear());
            }
        }

        Map<String, Object> completion = calculateCompletion(profile);
        profile.setProfileStatus((Boolean) completion.get("completed") ? "COMPLETE" : "INCOMPLETE");
        profile.setUpdatedAt(LocalDateTime.now());
        FellowshipProfile saved = fellowshipProfileRepository.save(profile);

        syncUserTable(user, saved);
        return saved;
    }

    public Map<String, Object> getCompletion(User user) {
        FellowshipProfile profile = getMyProfile(user);
        return calculateCompletion(profile);
    }

    public Map<String, Object> toResponse(FellowshipProfile profile) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("id", profile.getId());
        res.put("userId", profile.getUserId());
        res.put("nickname", safe(profile.getNickname()));
        res.put("gender", safe(profile.getGender()));
        res.put("birthYear", profile.getBirthYear());
        res.put("age", profile.getAge());
        res.put("city", safe(profile.getCity()));
        res.put("occupation", safe(profile.getOccupation()));
        res.put("education", safe(profile.getEducation()));
        res.put("height", profile.getHeight());
        res.put("bio", safe(profile.getBio()));
        res.put("intention", safe(profile.getIntention()));
        res.put("avatarUrl", safe(profile.getAvatarUrl()));
        res.put("tags", safe(profile.getTags()));
        res.put("profileStatus", profile.getProfileStatus());
        res.put("reviewStatus", profile.getReviewStatus());
        res.put("createdAt", profile.getCreatedAt());
        res.put("updatedAt", profile.getUpdatedAt());
        return res;
    }

    public Map<String, Object> calculateCompletion(FellowshipProfile profile) {
        List<String> missing = new ArrayList<>();
        checkMissing(missing, "nickname", profile.getNickname());
        checkMissing(missing, "gender", profile.getGender());
        if (profile.getBirthYear() == null) missing.add("birthYear");
        checkMissing(missing, "city", profile.getCity());
        checkMissing(missing, "occupation", profile.getOccupation());
        checkMissing(missing, "education", profile.getEducation());
        if (profile.getHeight() == null) missing.add("height");
        checkMissing(missing, "bio", profile.getBio());
        checkMissing(missing, "intention", profile.getIntention());
        checkMissing(missing, "avatarUrl", profile.getAvatarUrl());

        int total = 10;
        int filled = total - missing.size();
        int percent = Math.max(0, Math.min(100, (int) Math.round(filled * 100.0 / total)));
        boolean completed = missing.isEmpty();

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("completed", completed);
        res.put("percent", percent);
        res.put("missingFields", missing);
        return res;
    }

    private void syncUserTable(User user, FellowshipProfile profile) {
        user.setUsername(profile.getNickname());
        user.setProfilePhoto(profile.getAvatarUrl());
        user.setLocation(profile.getCity());
        user.setOccupation(profile.getOccupation());
        user.setBio(profile.getBio());
        user.setHeight(profile.getHeight());
        user.setAge(profile.getAge());
        user.setGender(convertGenderToInt(profile.getGender()));
        userRepository.save(user);
    }

    private void checkMissing(List<String> missing, String field, String value) {
        if (value == null || value.isBlank()) missing.add(field);
    }

    private String getString(Object value, String fallback) {
        if (value == null) return fallback;
        String s = String.valueOf(value).trim();
        return s.isBlank() ? fallback : s;
    }

    private Integer getInteger(Object value, Integer fallback) {
        if (value == null) return fallback;
        if (value instanceof Number n) return n.intValue();
        try {
            String text = String.valueOf(value).replace("cm", "").trim();
            return text.isBlank() ? fallback : Integer.parseInt(text);
        } catch (Exception e) {
            return fallback;
        }
    }

    private String normalizeTags(Object value, String fallback) {
        if (value == null) return fallback;
        if (value instanceof List<?> list) {
            List<String> tags = list.stream()
                    .map(String::valueOf)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
            return String.join(",", tags);
        }
        String text = String.valueOf(value).trim();
        return text.isBlank() ? fallback : text;
    }

    private String normalizeGender(String value) {
        if (value == null) return null;
        if ("1".equals(value) || "male".equalsIgnoreCase(value) || "男".equals(value)) return "male";
        if ("2".equals(value) || "female".equalsIgnoreCase(value) || "女".equals(value)) return "female";
        return value;
    }

    private String convertGender(Integer gender) {
        if (gender == null) return "";
        return gender == 1 ? "male" : "female";
    }

    private Integer convertGenderToInt(String gender) {
        if (gender == null || gender.isBlank()) return null;
        return "male".equalsIgnoreCase(gender) ? 1 : 2;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}

