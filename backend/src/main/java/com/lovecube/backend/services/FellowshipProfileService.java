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
import java.util.stream.Collectors;

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

        // identity role (defaults to "self" if absent or blank)
        String identityRole = getString(payload.get("identityRole"), profile.getIdentityRole());
        profile.setIdentityRole((identityRole == null || identityRole.isBlank()) ? "self" : identityRole);

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

        // Guardian-specific fields
        profile.setGuardianRole(getString(payload.get("guardianRole"), profile.getGuardianRole()));
        profile.setChildGender(getString(payload.get("childGender"), profile.getChildGender()));
        profile.setChildAge(getInteger(payload.get("childAge"), profile.getChildAge()));
        profile.setChildHeight(getInteger(payload.get("childHeight"), profile.getChildHeight()));
        profile.setChildEducation(getString(payload.get("childEducation"), profile.getChildEducation()));
        profile.setChildJob(getString(payload.get("childJob"), profile.getChildJob()));
        profile.setChildCity(getString(payload.get("childCity"), profile.getChildCity()));
        profile.setChildHouseCarStatus(getString(payload.get("childHouseCarStatus"), profile.getChildHouseCarStatus()));
        profile.setChildMarriageIntention(getString(payload.get("childMarriageIntention"), profile.getChildMarriageIntention()));
        profile.setChildPartnerRequirements(getString(payload.get("childPartnerRequirements"), profile.getChildPartnerRequirements()));
        if (payload.containsKey("guardianContactVisible")) {
            Object gcv = payload.get("guardianContactVisible");
            profile.setGuardianContactVisible(gcv == null ? null : Boolean.parseBoolean(String.valueOf(gcv)));
        }

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
        res.put("identityRole", profile.getIdentityRole() == null ? "self" : profile.getIdentityRole());
        res.put("guardianRole", safe(profile.getGuardianRole()));
        res.put("childGender", safe(profile.getChildGender()));
        res.put("childAge", profile.getChildAge());
        res.put("childHeight", profile.getChildHeight());
        res.put("childEducation", safe(profile.getChildEducation()));
        res.put("childJob", safe(profile.getChildJob()));
        res.put("childCity", safe(profile.getChildCity()));
        res.put("childHouseCarStatus", safe(profile.getChildHouseCarStatus()));
        res.put("childMarriageIntention", safe(profile.getChildMarriageIntention()));
        res.put("childPartnerRequirements", safe(profile.getChildPartnerRequirements()));
        res.put("guardianContactVisible", profile.getGuardianContactVisible() == null ? true : profile.getGuardianContactVisible());
        res.put("profileStatus", profile.getProfileStatus());
        res.put("reviewStatus", profile.getReviewStatus());
        res.put("createdAt", profile.getCreatedAt());
        res.put("updatedAt", profile.getUpdatedAt());
        return res;
    }

    public Map<String, Object> calculateCompletion(FellowshipProfile profile) {
        List<String> missing = new ArrayList<>();
        int total;

        boolean isGuardian = profile.getIdentityRole() != null && !profile.getIdentityRole().equals("self");

        if (isGuardian) {
            // Guardian completion: check child info + guardian identity
            checkMissing(missing, "nickname", profile.getNickname());
            checkMissing(missing, "guardianRole", profile.getGuardianRole());
            checkMissing(missing, "childGender", profile.getChildGender());
            if (profile.getChildAge() == null) missing.add("childAge");
            checkMissing(missing, "childCity", profile.getChildCity());
            checkMissing(missing, "childJob", profile.getChildJob());
            checkMissing(missing, "childEducation", profile.getChildEducation());
            checkMissing(missing, "childMarriageIntention", profile.getChildMarriageIntention());
            checkMissing(missing, "childPartnerRequirements", profile.getChildPartnerRequirements());
            total = 9;
        } else {
            // Self completion: check personal fields
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
            total = 10;
        }

        int filled = total - missing.size();
        int percent = Math.max(0, Math.min(100, (int) Math.round(filled * 100.0 / total)));

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("completed", missing.isEmpty());
        res.put("percent", percent);
        res.put("missingFields", missing);
        res.put("identityRole", profile.getIdentityRole() == null ? "self" : profile.getIdentityRole());
        return res;
    }

    public List<Map<String, Object>> listGuardianProfiles() {
        List<FellowshipProfile> profiles = fellowshipProfileRepository.findByIdentityRoleIn(
                List.of("guardian_son", "guardian_daughter"));
        return profiles.stream().map(this::toResponse).collect(Collectors.toList());
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

