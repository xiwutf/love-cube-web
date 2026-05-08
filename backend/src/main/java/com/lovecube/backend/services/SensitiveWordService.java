package com.lovecube.backend.services;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SensitiveWordService {

    // 有序 Map，确保替换时长词优先匹配
    private static final Map<String, String> WORD_MAP = new LinkedHashMap<>();

    static {
        WORD_MAP.put("弟兄姊妹", "伙伴");
        WORD_MAP.put("灵命成长", "个人成长");
        WORD_MAP.put("代祷清单", "心愿清单");
        WORD_MAP.put("信仰小组", "兴趣小组");
        WORD_MAP.put("祷告",    "祝福");
        WORD_MAP.put("祈祷",    "祝福");
        WORD_MAP.put("教会",    "团体");
        WORD_MAP.put("团契",    "小组");
        WORD_MAP.put("灵修",    "成长记录");
        WORD_MAP.put("见证",    "经历分享");
        WORD_MAP.put("代祷",    "互助支持");
        WORD_MAP.put("查经",    "学习小组");
        WORD_MAP.put("主内",    "圈子内");
        WORD_MAP.put("牧养",    "陪伴");
        WORD_MAP.put("敬拜",    "音乐活动");
        WORD_MAP.put("福音",    "好消息");
        WORD_MAP.put("蒙恩",    "收获");
        WORD_MAP.put("认罪",    "反思");
    }

    public Map<String, String> getWordMap() {
        return Collections.unmodifiableMap(WORD_MAP);
    }

    /** 返回文本中命中的所有敏感词（去重）。 */
    public List<String> detectHits(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();
        return WORD_MAP.keySet().stream()
                .filter(text::contains)
                .collect(Collectors.toList());
    }

    /** 0 → low，1-2 → medium，3+ → high */
    public String calculateRiskLevel(int hitCount) {
        if (hitCount == 0) return "low";
        if (hitCount <= 2)  return "medium";
        return "high";
    }
}
