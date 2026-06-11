package com.lovecube.backend.services;

import com.lovecube.backend.models.User;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 联谊资料完成度（迭代 1）：权重驱动完成度、曝光加成与权益说明。
 */
final class FellowshipProfileCompletion {

    private FellowshipProfileCompletion() {
    }

    record ItemDef(
            String key,
            String label,
            String benefitText,
            int weight,
            String route
    ) {
    }

    private static final List<ItemDef> ITEMS = List.of(
            new ItemDef("avatar", "上传头像", "上传头像，推荐曝光 +10%", 10, "/fellowship/profile/edit"),
            new ItemDef("age", "填写年龄", "填写年龄，匹配推荐更精准", 10, "/fellowship/profile/edit"),
            new ItemDef("city", "填写地区", "填写地区，提升同城匹配准确度", 10, "/fellowship/profile/edit"),
            new ItemDef("verification", "完成认证", "完成认证，获得认证标识并优先推荐", 20, "/fellowship/verify"),
            new ItemDef("photos", "上传生活照", "上传 3 张生活照，资料可信度 +20%", 20, "/fellowship/profile/edit"),
            new ItemDef("bio", "填写个人简介", "填写简介，让推荐对象更了解你", 10, "/fellowship/profile/edit"),
            new ItemDef("fellowship", "开通联谊", "开通联谊，进入推荐列表被更多人看到", 20, "/fellowship/profile/edit")
    );

    static Map<String, Object> build(User user, Map<String, Object> profile, int photoCount, int growthLevel) {
        boolean hasAvatar = isNotBlank(str(profile.get("avatarUrl")));
        boolean hasAge = profile.get("birthYear") != null
                || (profile.get("age") instanceof Number n && n.intValue() > 0);
        boolean hasCity = isNotBlank(str(profile.get("city")));
        boolean verified = Boolean.TRUE.equals(profile.get("photoVerified"))
                || Boolean.TRUE.equals(profile.get("realnameVerified"));
        boolean hasPhotos = photoCount >= 3;
        boolean hasBio = isNotBlank(str(profile.get("bio")));
        boolean fellowshipEnabled = user != null && Boolean.TRUE.equals(user.getFellowshipEnabled());

        Map<String, Boolean> doneMap = Map.of(
                "avatar", hasAvatar,
                "age", hasAge,
                "city", hasCity,
                "verification", verified,
                "photos", hasPhotos,
                "bio", hasBio,
                "fellowship", fellowshipEnabled
        );

        int completionRate = 0;
        List<Map<String, Object>> missingItems = new ArrayList<>();
        List<String> missingFields = new ArrayList<>();

        for (ItemDef item : ITEMS) {
            boolean done = Boolean.TRUE.equals(doneMap.get(item.key()));
            if (done) {
                completionRate += item.weight();
            } else {
                missingFields.add(item.key());
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("key", item.key());
                row.put("label", item.label());
                row.put("benefitText", item.benefitText());
                row.put("weight", item.weight());
                row.put("route", item.route());
                missingItems.add(row);
            }
        }

        completionRate = Math.max(0, Math.min(100, completionRate));
        int potentialRate = 100;
        int exposureBoostPercent = computeExposureBoostPercent(completionRate, verified, growthLevel);

        List<Map<String, Object>> unlockedBenefits = buildUnlockedBenefits(completionRate, verified);
        List<Map<String, Object>> nextBenefits = buildNextBenefits(completionRate, verified, missingItems.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("completionRate", completionRate);
        result.put("percent", completionRate);
        result.put("potentialRate", potentialRate);
        result.put("exposureBoostPercent", exposureBoostPercent);
        result.put("completed", completionRate >= 100);
        result.put("missingItems", missingItems);
        result.put("missingFields", missingFields);
        result.put("unlockedBenefits", unlockedBenefits);
        result.put("nextBenefits", nextBenefits);
        result.put("verified", verified);
        result.put("photoCount", photoCount);
        result.put("growthLevel", growthLevel);
        result.put("identityRole", profile.getOrDefault("identityRole", "self"));
        return result;
    }

    static int computeExposureBoostPercent(int completionRate, boolean verified, int growthLevel) {
        int boost = (int) Math.round(Math.max(0, Math.min(100, completionRate)) * 0.3);
        if (verified) {
            boost += 20;
        }
        int lv = growthLevel <= 0 ? 1 : growthLevel;
        boost += Math.min(15, Math.max(0, (lv - 1) * 3));
        return Math.min(80, boost);
    }

    static double computeRecommendRank(int completionRate, boolean verified, int growthLevel) {
        double score = Math.max(0, Math.min(100, completionRate)) * 0.3;
        if (verified) {
            score += 20;
        }
        int lv = growthLevel <= 0 ? 1 : growthLevel;
        score += Math.min(15, Math.max(0, (lv - 1) * 3));
        return score;
    }

    private static List<Map<String, Object>> buildUnlockedBenefits(int completionRate, boolean verified) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (verified) {
            rows.add(benefit("verified_badge", "专属认证标识", "你的资料已展示认证标识"));
            rows.add(benefit("verified_boost", "认证推荐加成", "认证用户在推荐排序中优先展示"));
        }
        if (completionRate >= 40) {
            rows.add(benefit("pool", "联谊推荐曝光", "已进入联谊推荐列表，可被更多用户看到"));
        }
        if (completionRate >= 60) {
            rows.add(benefit("swipe", "滑卡次数提升", "资料更完整，每日可浏览更多推荐"));
        }
        if (completionRate >= 80) {
            rows.add(benefit("boost", "推荐加权", "你的资料在推荐列表中排序更靠前"));
        }
        if (completionRate >= 100) {
            rows.add(benefit("full", "资料达人曝光", "资料完整，曝光加成已拉满"));
        }
        return rows;
    }

    private static List<Map<String, Object>> buildNextBenefits(int completionRate, boolean verified, int missingCount) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (!verified) {
            rows.add(benefit("verify_next", "完成认证", "获得认证标识，推荐权重 +20%"));
        }
        if (completionRate < 40) {
            rows.add(benefit("tier40", "资料达 40%", "解锁联谊推荐列表曝光"));
        } else if (completionRate < 60) {
            rows.add(benefit("tier60", "资料达 60%", "解锁更多每日滑卡次数"));
        } else if (completionRate < 80) {
            rows.add(benefit("tier80", "资料达 80%", "解锁推荐加权，优先被更多人看到"));
        } else if (completionRate < 100) {
            rows.add(benefit("tier100", "资料达 100%", "曝光加成拉满，匹配推荐力最强"));
        }
        if (missingCount > 0 && completionRate < 100) {
            Map<String, Object> hint = new LinkedHashMap<>();
            hint.put("key", "missing_hint");
            hint.put("title", "再补 " + missingCount + " 项");
            hint.put("description", "即可解锁下一档推荐权益");
            rows.add(hint);
        }
        return rows;
    }

    private static Map<String, Object> benefit(String key, String title, String description) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("key", key);
        row.put("title", title);
        row.put("description", description);
        return row;
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
