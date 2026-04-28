package com.lovecube.backend.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovecube.backend.entity.FellowshipProfile;
import com.lovecube.backend.entity.FellowshipProfileMain;
import com.lovecube.backend.entity.UserPhoto;
import com.lovecube.backend.entity.UserProfile;
import com.lovecube.backend.entity.UserVerification;
import com.lovecube.backend.entity.VerificationRequest;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.FellowshipProfileMainRepository;
import com.lovecube.backend.repository.FellowshipProfileRepository;
import com.lovecube.backend.repository.UserPhotoRepository;
import com.lovecube.backend.repository.UserProfileRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.repository.UserVerificationRepository;
import com.lovecube.backend.repository.VerificationRequestRepository;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UnifiedProfileService {
    private static final int NICKNAME_MAX_LENGTH = 20;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final FellowshipProfileRepository legacyFellowshipProfileRepository;
    private final FellowshipProfileMainRepository fellowshipProfileMainRepository;
    private final UserPhotoRepository userPhotoRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final VerificationRequestRepository verificationRequestRepository;
    private final VerificationService verificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UnifiedProfileService(
            UserRepository userRepository,
            UserProfileRepository userProfileRepository,
            FellowshipProfileRepository legacyFellowshipProfileRepository,
            FellowshipProfileMainRepository fellowshipProfileMainRepository,
            UserPhotoRepository userPhotoRepository,
            UserVerificationRepository userVerificationRepository,
            VerificationRequestRepository verificationRequestRepository,
            VerificationService verificationService
    ) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.legacyFellowshipProfileRepository = legacyFellowshipProfileRepository;
        this.fellowshipProfileMainRepository = fellowshipProfileMainRepository;
        this.userPhotoRepository = userPhotoRepository;
        this.userVerificationRepository = userVerificationRepository;
        this.verificationRequestRepository = verificationRequestRepository;
        this.verificationService = verificationService;
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

    public Map<String, Object> buildPublicProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return buildUnifiedProfile(user);
    }

    public Map<String, Object> buildCurrentProfile(User user) {
        return buildUnifiedProfile(user);
    }

    public Map<String, Object> buildLegacyUserPayload(User user) {
        Map<String, Object> merged = buildUnifiedProfile(user);
        Map<String, Object> result = new LinkedHashMap<>();
        Long userId = user.getUserid();

        result.put("userId", userId);
        result.put("id", userId);
        result.put("username", merged.getOrDefault("nickname", ""));
        result.put("nickname", merged.getOrDefault("nickname", ""));
        result.put("phone", user.getPhoneNumber());
        result.put("phoneNumber", user.getPhoneNumber());
        result.put("gender", toChineseGender((String) merged.get("gender")));
        result.put("location", merged.getOrDefault("location", ""));
        result.put("occupation", merged.getOrDefault("occupation", ""));
        result.put("height", merged.get("height"));
        result.put("signature", merged.getOrDefault("bio", ""));
        result.put("bio", merged.getOrDefault("bio", ""));
        result.put("profilePhoto", merged.getOrDefault("avatar", ""));
        result.put("avatar", merged.getOrDefault("avatar", ""));
        result.put("photos", merged.getOrDefault("photos", List.of()));
        result.put("role", normalizeRole(user.getRole()));
        result.put("status", "DISABLED".equalsIgnoreCase(user.getUserStatus()) ? "disabled" : "active");
        result.put("fellowshipEnabled", Boolean.TRUE.equals(user.getFellowshipEnabled()));
        result.put("fellowshipMatchVisible", Boolean.TRUE.equals(user.getFellowshipMatchVisible()));
        result.put("verificationStatus", merged.getOrDefault("verificationStatus", "none"));
        result.put("verificationRejectReason", merged.getOrDefault("verificationRejectReason", ""));
        result.put("photoVerified", merged.getOrDefault("photoVerified", false));
        result.put("realnameVerified", merged.getOrDefault("realnameVerified", false));
        result.put("completionRate", merged.getOrDefault("completionRate", 0));
        result.put("age", merged.get("age"));
        result.put("constellation", merged.getOrDefault("constellation", ""));
        result.put("birthday", merged.getOrDefault("birthday", ""));
        result.put("birthDate", merged.get("birthDate"));
        return result;
    }

    @Transactional
    public Map<String, Object> updateLegacyProfile(User user, Map<String, Object> payload) {
        if (payload.containsKey("nickname") || payload.containsKey("username")) {
            String nickname = firstText(payload.get("nickname"), payload.get("username"));
            if (nickname != null) {
                user.setUsername(normalizeNickname(nickname));
            }
        }
        if (payload.containsKey("avatar") || payload.containsKey("profilePhoto")) {
            String avatar = firstText(payload.get("avatar"), payload.get("profilePhoto"));
            if (avatar != null) {
                user.setProfilePhoto(avatar);
            }
        }
        if (payload.containsKey("gender")) {
            user.setGender(toGenderCode(payload.get("gender")));
        }
        if (payload.containsKey("birthday")) {
            LocalDate birthday = parseDate(payload.get("birthday"));
            user.setBirthDate(birthday == null ? null : birthday.atStartOfDay());
            user.setAge(birthday == null ? user.getAge() : calcAge(birthday));
        }
        if (payload.containsKey("location")) {
            user.setLocation(toTrimmed(payload.get("location")));
        }
        if (payload.containsKey("occupation")) {
            user.setOccupation(toTrimmed(payload.get("occupation")));
        }
        if (payload.containsKey("height")) {
            user.setHeight(toInteger(payload.get("height")));
        }
        if (payload.containsKey("signature") || payload.containsKey("bio")) {
            user.setBio(firstText(payload.get("signature"), payload.get("bio")));
        }
        userRepository.save(user);

        if (payload.containsKey("photos") && payload.get("photos") instanceof List<?> list) {
            replaceUserPhotos(user.getUserid(), list.stream().map(String::valueOf).collect(Collectors.toList()));
        }
        syncMainProfileFromUser(user);
        syncUserProfileFromUser(user);
        syncLegacyFellowshipProfileFromUser(user);

        return buildLegacyUserPayload(user);
    }

    @Transactional
    public Map<String, Object> updateFellowshipProfile(User user, Map<String, Object> payload) {
        FellowshipProfileMain main = fellowshipProfileMainRepository.findByUserId(user.getUserid())
                .orElseGet(() -> {
                    FellowshipProfileMain created = new FellowshipProfileMain();
                    created.setUserId(user.getUserid());
                    created.setReportedCount(0);
                    return created;
                });
        FellowshipProfile legacy = legacyFellowshipProfileRepository.findByUserId(user.getUserid())
                .orElseGet(() -> {
                    FellowshipProfile created = new FellowshipProfile();
                    created.setUserId(user.getUserid());
                    created.setGuardianContactVisible(true);
                    return created;
                });

        String nickname = getText(payload, "nickname", main.getNickname(), user.getUsername());
        nickname = normalizeNickname(nickname);
        String gender = normalizeGender(getText(payload, "gender", main.getGender(), fromGenderCode(user.getGender())));
        Integer birthYear = getInteger(payload, "birthYear", legacy.getBirthYear(), user.getBirthDate() == null ? null : user.getBirthDate().getYear());
        Integer age = birthYear == null ? user.getAge() : calcAge(birthYear);
        String city = getText(payload, "city", main.getCity(), user.getLocation());
        String occupation = getText(payload, "occupation", main.getOccupation(), user.getOccupation());
        String education = getText(payload, "education", legacy.getEducation(), null);
        Integer height = getInteger(payload, "height", main.getHeight(), user.getHeight());
        String bio = getText(payload, "bio", main.getBio(), user.getBio());
        String intention = getText(payload, "intention", legacy.getIntention(), null);
        String avatarUrl = getText(payload, "avatarUrl", main.getAvatar(), user.getProfilePhoto());
        String tags = normalizeTags(payload.get("tags"), legacy.getTags());

        main.setNickname(nickname);
        main.setGender(gender);
        main.setBirthday(birthYear == null ? null : LocalDate.of(birthYear, 1, 1));
        main.setCity(city);
        main.setOccupation(occupation);
        main.setHeight(height);
        main.setBio(bio);
        main.setAvatar(avatarUrl);
        main.setCompletionRate(calculateCompletionRateForFellowship(nickname, gender, birthYear, city, occupation, education, height, bio, intention, avatarUrl));
        main.setReviewStatus(defaultText(main.getReviewStatus(), "pending"));
        main.setVerificationStatus(defaultText(main.getVerificationStatus(), "none"));
        main.setLastActiveAt(LocalDateTime.now());
        fellowshipProfileMainRepository.save(main);

        legacy.setNickname(nickname);
        legacy.setGender(gender);
        legacy.setBirthYear(birthYear);
        legacy.setAge(age);
        legacy.setCity(city);
        legacy.setOccupation(occupation);
        legacy.setEducation(education);
        legacy.setHeight(height);
        legacy.setBio(bio);
        legacy.setIntention(intention);
        legacy.setAvatarUrl(avatarUrl);
        legacy.setTags(tags);
        legacy.setIdentityRole(getText(payload, "identityRole", legacy.getIdentityRole(), "self"));
        legacy.setGuardianRole(getText(payload, "guardianRole", legacy.getGuardianRole(), null));
        legacy.setChildGender(getText(payload, "childGender", legacy.getChildGender(), null));
        legacy.setChildAge(getInteger(payload, "childAge", legacy.getChildAge(), null));
        legacy.setChildHeight(getInteger(payload, "childHeight", legacy.getChildHeight(), null));
        legacy.setChildEducation(getText(payload, "childEducation", legacy.getChildEducation(), null));
        legacy.setChildJob(getText(payload, "childJob", legacy.getChildJob(), null));
        legacy.setChildCity(getText(payload, "childCity", legacy.getChildCity(), null));
        legacy.setChildHouseCarStatus(getText(payload, "childHouseCarStatus", legacy.getChildHouseCarStatus(), null));
        legacy.setChildMarriageIntention(getText(payload, "childMarriageIntention", legacy.getChildMarriageIntention(), null));
        legacy.setChildPartnerRequirements(getText(payload, "childPartnerRequirements", legacy.getChildPartnerRequirements(), null));
        if (payload.containsKey("guardianContactVisible")) {
            legacy.setGuardianContactVisible(payload.get("guardianContactVisible") == null
                    ? null
                    : Boolean.parseBoolean(String.valueOf(payload.get("guardianContactVisible"))));
        }
        legacy.setProfileStatus(main.getCompletionRate() >= 100 ? "COMPLETE" : "INCOMPLETE");
        legacy.setReviewStatus(defaultText(legacy.getReviewStatus(), "PENDING"));
        legacyFellowshipProfileRepository.save(legacy);

        user.setUsername(nickname);
        user.setProfilePhoto(avatarUrl);
        user.setLocation(city);
        user.setOccupation(occupation);
        user.setBio(bio);
        user.setHeight(height);
        user.setAge(age);
        user.setGender(toGenderCode(gender));
        user.setBirthDate(birthYear == null ? null : LocalDate.of(birthYear, 1, 1).atStartOfDay());
        userRepository.save(user);
        syncUserProfileFromUser(user);

        return buildFellowshipPayload(user);
    }

    public Map<String, Object> buildFellowshipPayload(User user) {
        Map<String, Object> merged = buildUnifiedProfile(user);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getUserid());
        result.put("nickname", merged.getOrDefault("nickname", ""));
        result.put("gender", merged.getOrDefault("gender", ""));
        result.put("birthYear", merged.get("birthYear"));
        result.put("age", merged.get("age"));
        result.put("city", merged.getOrDefault("location", ""));
        result.put("occupation", merged.getOrDefault("occupation", ""));
        result.put("education", merged.getOrDefault("education", ""));
        result.put("height", merged.get("height"));
        result.put("bio", merged.getOrDefault("bio", ""));
        result.put("intention", merged.getOrDefault("intention", ""));
        result.put("avatarUrl", merged.getOrDefault("avatar", ""));
        result.put("tags", merged.getOrDefault("tags", ""));
        result.put("identityRole", merged.getOrDefault("identityRole", "self"));
        result.put("guardianRole", merged.getOrDefault("guardianRole", ""));
        result.put("childGender", merged.getOrDefault("childGender", ""));
        result.put("childAge", merged.get("childAge"));
        result.put("childHeight", merged.get("childHeight"));
        result.put("childEducation", merged.getOrDefault("childEducation", ""));
        result.put("childJob", merged.getOrDefault("childJob", ""));
        result.put("childCity", merged.getOrDefault("childCity", ""));
        result.put("childHouseCarStatus", merged.getOrDefault("childHouseCarStatus", ""));
        result.put("childMarriageIntention", merged.getOrDefault("childMarriageIntention", ""));
        result.put("childPartnerRequirements", merged.getOrDefault("childPartnerRequirements", ""));
        result.put("guardianContactVisible", merged.getOrDefault("guardianContactVisible", true));
        result.put("profileStatus", merged.getOrDefault("profileStatus", "INCOMPLETE"));
        result.put("reviewStatus", merged.getOrDefault("reviewStatus", "pending"));
        result.put("completionRate", merged.getOrDefault("completionRate", 0));
        result.put("fellowshipEnabled", Boolean.TRUE.equals(user.getFellowshipEnabled()));
        result.put("fellowshipMatchVisible", Boolean.TRUE.equals(user.getFellowshipMatchVisible()));
        return result;
    }

    public Map<String, Object> buildFellowshipCompletion(User user) {
        Map<String, Object> p = buildFellowshipPayload(user);
        List<String> missing = new ArrayList<>();
        checkMissing(missing, "nickname", p.get("nickname"));
        checkMissing(missing, "gender", p.get("gender"));
        if (p.get("birthYear") == null) missing.add("birthYear");
        checkMissing(missing, "city", p.get("city"));
        checkMissing(missing, "occupation", p.get("occupation"));
        checkMissing(missing, "education", p.get("education"));
        if (p.get("height") == null) missing.add("height");
        checkMissing(missing, "bio", p.get("bio"));
        checkMissing(missing, "intention", p.get("intention"));
        checkMissing(missing, "avatarUrl", p.get("avatarUrl"));
        int total = 10;
        int percent = Math.max(0, Math.min(100, (int) Math.round((total - missing.size()) * 100.0 / total)));
        return Map.of(
                "completed", missing.isEmpty(),
                "percent", percent,
                "missingFields", missing,
                "identityRole", p.getOrDefault("identityRole", "self")
        );
    }

    public List<String> getUserPhotos(Long userId) {
        List<String> fromTable = userPhotoRepository.findByUserIdOrderBySortOrderAscIdAsc(userId).stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()))
                .map(UserPhoto::getPhotoUrl)
                .filter(Objects::nonNull)
                .toList();
        if (!fromTable.isEmpty()) {
            return fromTable;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return List.of();
        }
        return parsePhotosJson(user.getPhotos());
    }

    @Transactional
    public void replaceUserPhotos(Long userId, List<String> photoUrls) {
        List<String> cleaned = (photoUrls == null ? List.<String>of() : photoUrls).stream()
                .map(this::toTrimmed)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .collect(Collectors.toList());

        userPhotoRepository.deleteByUserId(userId);
        for (int i = 0; i < cleaned.size(); i++) {
            UserPhoto row = new UserPhoto();
            row.setUserId(userId);
            row.setPhotoUrl(cleaned.get(i));
            row.setSortOrder(i);
            row.setPrimary(i == 0);
            row.setStatus("ACTIVE");
            userPhotoRepository.save(row);
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setPhotos(writePhotosJson(cleaned));
            userRepository.save(user);
        }

        FellowshipProfileMain main = fellowshipProfileMainRepository.findByUserId(userId).orElse(null);
        if (main != null) {
            main.setPhotosJson(writePhotosJson(cleaned));
            fellowshipProfileMainRepository.save(main);
        }
    }

    private Map<String, Object> buildUnifiedProfile(User user) {
        UserProfile userProfile = userProfileRepository.findByUserId(user.getUserid()).orElse(null);
        FellowshipProfileMain main = fellowshipProfileMainRepository.findByUserId(user.getUserid()).orElse(null);
        FellowshipProfile legacy = legacyFellowshipProfileRepository.findByUserId(user.getUserid()).orElse(null);
        Optional<UserVerification> uv = userVerificationRepository.findTopByUserIdOrderBySubmittedAtDesc(user.getUserid());
        Optional<VerificationRequest> lv = verificationRequestRepository
                .findByUserIdOrderBySubmittedAtDesc(user.getUserid()).stream().findFirst();
        List<String> photos = getUserPhotos(user.getUserid());

        String nickname = firstNonBlank(
                legacy == null ? null : legacy.getNickname(),
                main == null ? null : main.getNickname(),
                userProfile == null ? null : userProfile.getNickname(),
                user.getUsername()
        );
        String avatar = firstNonBlank(
                legacy == null ? null : legacy.getAvatarUrl(),
                main == null ? null : main.getAvatar(),
                userProfile == null ? null : userProfile.getAvatar(),
                user.getProfilePhoto()
        );
        String gender = normalizeGender(firstNonBlank(
                legacy == null ? null : legacy.getGender(),
                main == null ? null : main.getGender(),
                userProfile == null ? null : userProfile.getGender(),
                fromGenderCode(user.getGender())
        ));
        Integer birthYear = firstNonNull(
                legacy == null ? null : legacy.getBirthYear(),
                main != null && main.getBirthday() != null ? main.getBirthday().getYear() : null,
                user.getBirthDate() == null ? null : user.getBirthDate().getYear()
        );
        Integer age = firstNonNull(
                legacy == null ? null : legacy.getAge(),
                user.getAge(),
                birthYear == null ? null : calcAge(birthYear)
        );
        String city = firstNonBlank(
                legacy == null ? null : legacy.getCity(),
                main == null ? null : main.getCity(),
                userProfile == null ? null : userProfile.getCity(),
                user.getLocation()
        );
        String occupation = firstNonBlank(
                legacy == null ? null : legacy.getOccupation(),
                main == null ? null : main.getOccupation(),
                user.getOccupation()
        );
        Integer height = firstNonNull(
                legacy == null ? null : legacy.getHeight(),
                main == null ? null : main.getHeight(),
                user.getHeight()
        );
        String bio = firstNonBlank(
                legacy == null ? null : legacy.getBio(),
                main == null ? null : main.getBio(),
                user.getBio()
        );
        String education = firstNonBlank(legacy == null ? null : legacy.getEducation(), userProfile == null ? null : userProfile.getEducation());
        String intention = firstNonBlank(legacy == null ? null : legacy.getIntention());
        String tags = firstNonBlank(legacy == null ? null : legacy.getTags());

        String verificationStatus = uv.map(UserVerification::getStatus).orElseGet(() ->
                lv.map(VerificationRequest::getStatus).orElse("none"));
        String verificationRejectReason = uv.map(UserVerification::getRejectReason).orElseGet(() ->
                lv.map(VerificationRequest::getRejectReason).orElse(""));
        Map<String, Object> verifyBadges = verificationService.getStatusSummary(user.getUserid());
        boolean photoVerified   = Boolean.TRUE.equals(verifyBadges.get("photoVerified"));
        boolean realnameVerified = Boolean.TRUE.equals(verifyBadges.get("realnameVerified"));
        String reviewStatus = firstNonBlank(
                legacy == null ? null : legacy.getReviewStatus(),
                main == null ? null : main.getReviewStatus(),
                "pending"
        );
        String profileStatus = firstNonBlank(
                legacy == null ? null : legacy.getProfileStatus(),
                main != null && main.getCompletionRate() != null && main.getCompletionRate() >= 100 ? "COMPLETE" : "INCOMPLETE"
        );

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("userId", user.getUserid());
        out.put("nickname", defaultText(nickname, ""));
        out.put("avatar", defaultText(avatar, ""));
        out.put("gender", defaultText(gender, ""));
        out.put("birthYear", birthYear);
        out.put("birthday", birthYear == null ? "" : birthYear + "-01-01");
        out.put("birthDate", user.getBirthDate());
        out.put("age", age);
        out.put("constellation", user.getBirthDate() == null ? "" : getConstellation(user.getBirthDate().toLocalDate()));
        out.put("location", defaultText(city, ""));
        out.put("occupation", defaultText(occupation, ""));
        out.put("height", height);
        out.put("bio", defaultText(bio, ""));
        out.put("education", defaultText(education, ""));
        out.put("intention", defaultText(intention, ""));
        out.put("tags", defaultText(tags, ""));
        out.put("photos", photos);
        out.put("completionRate", firstNonNull(main == null ? null : main.getCompletionRate(), 0));
        out.put("verificationStatus", defaultText(verificationStatus, "none"));
        out.put("verificationRejectReason", defaultText(verificationRejectReason, ""));
        out.put("photoVerified", photoVerified);
        out.put("realnameVerified", realnameVerified);
        out.put("reviewStatus", defaultText(reviewStatus, "pending"));
        out.put("profileStatus", defaultText(profileStatus, "INCOMPLETE"));

        out.put("identityRole", legacy == null ? "self" : defaultText(legacy.getIdentityRole(), "self"));
        out.put("guardianRole", legacy == null ? "" : defaultText(legacy.getGuardianRole(), ""));
        out.put("childGender", legacy == null ? "" : defaultText(legacy.getChildGender(), ""));
        out.put("childAge", legacy == null ? null : legacy.getChildAge());
        out.put("childHeight", legacy == null ? null : legacy.getChildHeight());
        out.put("childEducation", legacy == null ? "" : defaultText(legacy.getChildEducation(), ""));
        out.put("childJob", legacy == null ? "" : defaultText(legacy.getChildJob(), ""));
        out.put("childCity", legacy == null ? "" : defaultText(legacy.getChildCity(), ""));
        out.put("childHouseCarStatus", legacy == null ? "" : defaultText(legacy.getChildHouseCarStatus(), ""));
        out.put("childMarriageIntention", legacy == null ? "" : defaultText(legacy.getChildMarriageIntention(), ""));
        out.put("childPartnerRequirements", legacy == null ? "" : defaultText(legacy.getChildPartnerRequirements(), ""));
        out.put("guardianContactVisible", legacy == null ? true : legacy.getGuardianContactVisible() == null || legacy.getGuardianContactVisible());
        return out;
    }

    private void syncMainProfileFromUser(User user) {
        FellowshipProfileMain main = fellowshipProfileMainRepository.findByUserId(user.getUserid()).orElseGet(() -> {
            FellowshipProfileMain created = new FellowshipProfileMain();
            created.setUserId(user.getUserid());
            return created;
        });
        main.setNickname(user.getUsername());
        main.setAvatar(user.getProfilePhoto());
        main.setGender(fromGenderCode(user.getGender()));
        main.setBirthday(user.getBirthDate() == null ? null : user.getBirthDate().toLocalDate());
        main.setCity(user.getLocation());
        main.setOccupation(user.getOccupation());
        main.setHeight(user.getHeight());
        main.setBio(user.getBio());
        main.setPhotosJson(user.getPhotos());
        main.setLastActiveAt(LocalDateTime.now());
        fellowshipProfileMainRepository.save(main);
    }

    private void syncUserProfileFromUser(User user) {
        UserProfile p = userProfileRepository.findByUserId(user.getUserid()).orElseGet(() -> {
            UserProfile created = new UserProfile();
            created.setUserId(user.getUserid());
            return created;
        });
        if (user.getUsername() != null) p.setNickname(user.getUsername());
        if (user.getProfilePhoto() != null) p.setAvatar(user.getProfilePhoto());
        if (user.getAge() != null) p.setAge(user.getAge());
        if (user.getGender() != null) p.setGender(fromGenderCode(user.getGender()));
        p.setCity(user.getLocation());
        p.setEducation(p.getEducation());
        p.setLastActiveTime(LocalDateTime.now());
        userProfileRepository.save(p);
    }

    private void syncLegacyFellowshipProfileFromUser(User user) {
        FellowshipProfile p = legacyFellowshipProfileRepository.findByUserId(user.getUserid()).orElseGet(() -> {
            FellowshipProfile created = new FellowshipProfile();
            created.setUserId(user.getUserid());
            return created;
        });
        p.setNickname(user.getUsername());
        p.setAvatarUrl(user.getProfilePhoto());
        p.setGender(fromGenderCode(user.getGender()));
        p.setAge(user.getAge());
        p.setBirthYear(user.getBirthDate() == null ? null : user.getBirthDate().getYear());
        p.setCity(user.getLocation());
        p.setOccupation(user.getOccupation());
        p.setHeight(user.getHeight());
        p.setBio(user.getBio());
        p.setProfileStatus(defaultText(p.getProfileStatus(), "INCOMPLETE"));
        p.setReviewStatus(defaultText(p.getReviewStatus(), "PENDING"));
        legacyFellowshipProfileRepository.save(p);
    }

    private String toChineseGender(String normalized) {
        if ("male".equalsIgnoreCase(normalized)) return "男";
        if ("female".equalsIgnoreCase(normalized)) return "女";
        return "";
    }

    private String normalizeRole(String role) {
        String r = role == null ? "" : role.trim().toLowerCase(Locale.ROOT);
        if ("admin".equals(r) || "super_admin".equals(r) || "root".equals(r)) return "admin";
        return "user";
    }

    private Integer toGenderCode(Object raw) {
        if (raw == null) return null;
        String v = String.valueOf(raw).trim().toLowerCase(Locale.ROOT);
        if ("1".equals(v) || "male".equals(v) || "男".equals(v)) return 1;
        if ("2".equals(v) || "female".equals(v) || "女".equals(v)) return 2;
        return null;
    }

    private String fromGenderCode(Integer code) {
        if (code == null) return "";
        return code == 1 ? "male" : "female";
    }

    private String normalizeGender(String value) {
        if (value == null || value.isBlank()) return "";
        return "female".equalsIgnoreCase(value) || "女".equals(value) || "2".equals(value) ? "female" : "male";
    }

    private LocalDate parseDate(Object raw) {
        String text = toTrimmed(raw);
        if (text == null || text.isBlank()) return null;
        try {
            return LocalDate.parse(text);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer calcAge(LocalDate birthday) {
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    private Integer calcAge(int birthYear) {
        return LocalDate.now().getYear() - birthYear;
    }

    private String getConstellation(LocalDate birthDate) {
        if (birthDate == null) return "";
        int month = birthDate.getMonthValue();
        int day = birthDate.getDayOfMonth();
        if ((month == 1 && day >= 20) || (month == 2 && day <= 18)) return "水瓶座";
        if ((month == 2 && day >= 19) || (month == 3 && day <= 20)) return "双鱼座";
        if ((month == 3 && day >= 21) || (month == 4 && day <= 19)) return "白羊座";
        if ((month == 4 && day >= 20) || (month == 5 && day <= 20)) return "金牛座";
        if ((month == 5 && day >= 21) || (month == 6 && day <= 21)) return "双子座";
        if ((month == 6 && day >= 22) || (month == 7 && day <= 22)) return "巨蟹座";
        if ((month == 7 && day >= 23) || (month == 8 && day <= 22)) return "狮子座";
        if ((month == 8 && day >= 23) || (month == 9 && day <= 22)) return "处女座";
        if ((month == 9 && day >= 23) || (month == 10 && day <= 23)) return "天秤座";
        if ((month == 10 && day >= 24) || (month == 11 && day <= 22)) return "天蝎座";
        if ((month == 11 && day >= 23) || (month == 12 && day <= 21)) return "射手座";
        return "摩羯座";
    }

    private int calculateCompletionRateForFellowship(
            String nickname,
            String gender,
            Integer birthYear,
            String city,
            String occupation,
            String education,
            Integer height,
            String bio,
            String intention,
            String avatar
    ) {
        int total = 10;
        int done = 0;
        if (isNotBlank(nickname)) done++;
        if (isNotBlank(gender)) done++;
        if (birthYear != null) done++;
        if (isNotBlank(city)) done++;
        if (isNotBlank(occupation)) done++;
        if (isNotBlank(education)) done++;
        if (height != null) done++;
        if (isNotBlank(bio)) done++;
        if (isNotBlank(intention)) done++;
        if (isNotBlank(avatar)) done++;
        return (int) Math.round(done * 100.0 / total);
    }

    private Integer getInteger(Map<String, Object> payload, String key, Integer current, Integer fallback) {
        if (!payload.containsKey(key) || payload.get(key) == null) {
            return firstNonNull(current, fallback);
        }
        return toInteger(payload.get(key));
    }

    private Integer toInteger(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(raw).replace("cm", "").trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private String getText(Map<String, Object> payload, String key, String current, String fallback) {
        if (!payload.containsKey(key)) {
            return firstNonBlank(current, fallback);
        }
        String value = toTrimmed(payload.get(key));
        return value == null ? firstNonBlank(current, fallback) : value;
    }

    private String normalizeTags(Object incoming, String fallback) {
        if (incoming == null) return fallback;
        if (incoming instanceof List<?> list) {
            return list.stream().map(String::valueOf).map(String::trim).filter(this::isNotBlank).collect(Collectors.joining(","));
        }
        String text = toTrimmed(incoming);
        return text == null ? fallback : text;
    }

    private String firstText(Object a, Object b) {
        String av = toTrimmed(a);
        if (isNotBlank(av)) return av;
        return toTrimmed(b);
    }

    private String toTrimmed(Object raw) {
        if (raw == null) return null;
        String t = String.valueOf(raw).trim();
        return t.isBlank() ? null : t;
    }

    private boolean isNotBlank(String text) {
        return text != null && !text.isBlank();
    }

    @SafeVarargs
    private <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) return value;
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }

    private String defaultText(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text;
    }

    private String normalizeNickname(String rawNickname) {
        if (rawNickname == null) {
            return null;
        }
        String nickname = rawNickname.trim();
        if (nickname.length() > NICKNAME_MAX_LENGTH) {
            throw new IllegalArgumentException("昵称最多 20 个字符");
        }
        return nickname;
    }

    private List<String> parsePhotosJson(String photosJson) {
        if (photosJson == null || photosJson.trim().isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(photosJson, new TypeReference<List<String>>() {});
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private String writePhotosJson(List<String> urls) {
        try {
            return objectMapper.writeValueAsString(urls);
        } catch (Exception e) {
            return "[]";
        }
    }

    private void checkMissing(List<String> missing, String key, Object value) {
        if (value == null) {
            missing.add(key);
            return;
        }
        if (value instanceof String s && s.isBlank()) {
            missing.add(key);
        }
    }
}
