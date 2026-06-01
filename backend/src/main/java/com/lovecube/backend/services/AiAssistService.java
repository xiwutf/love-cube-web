package com.lovecube.backend.services;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 规则型 AI 助手（无外部大模型依赖）：资料润色与破冰话术建议。
 */
@Service
public class AiAssistService {

    public Map<String, Object> polishProfile(String bio, String nickname) {
        String base = bio == null ? "" : bio.trim();
        String name = nickname == null || nickname.isBlank() ? "我" : nickname.trim();
        List<String> suggestions = new ArrayList<>();
        if (base.isBlank()) {
            suggestions.add(name + "，喜欢探索城市里的新去处，也享受安静阅读的时光。期待认识聊得来的伙伴。");
            suggestions.add("平时工作节奏稳定，周末会运动或尝试新餐厅。希望找到价值观相近、能一起成长的朋友。");
            suggestions.add("性格偏真诚直接，重视沟通。如果你也相信「慢慢来会比较快」，欢迎打个招呼。");
        } else {
            suggestions.add(enhance(base, "真诚"));
            suggestions.add(enhance(base, "活泼"));
            suggestions.add(enhance(base, "简洁"));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("suggestions", suggestions);
        result.put("tips", List.of(
                "补充 1 个具体爱好比空泛形容词更有记忆点",
                "避免过长，80-120 字在移动端阅读体验最佳",
                "可加入你期待的关系节奏（慢热/直接）"
        ));
        return result;
    }

    public Map<String, Object> icebreakerHints(String scene) {
        String key = scene == null ? "match" : scene.trim().toLowerCase();
        List<String> phrases = switch (key) {
            case "help" -> List.of(
                    "你好，我看到你的需求了，我在这方面有一点经验，方便聊聊具体情况吗？",
                    "我刚好有相关资源，可以先发你参考，不合适也没关系～",
                    "如果不介意，我们可以先语音 5 分钟对齐一下需求。"
            );
            case "event" -> List.of(
                    "你好，我们好像报了同一个活动，现场要不要一起签到？",
                    "第一次参加这类活动有点紧张，你以前来过吗？",
                    "活动结束如果有空，可以一起交流一下感受。"
            );
            default -> List.of(
                    "你好，看到你的资料觉得挺投缘的，你平时周末一般怎么安排？",
                    "我对你的介绍里提到的爱好很感兴趣，能多聊两句吗？",
                    "如果不打扰的话，想听听你最近最开心的一件事～"
            );
        };
        return Map.of("scene", key, "phrases", phrases);
    }

    private String enhance(String base, String tone) {
        return switch (tone) {
            case "活泼" -> base + " 平时也爱尝试新鲜事物，觉得生活要有烟火气也有小惊喜。";
            case "简洁" -> base.length() > 60 ? base.substring(0, 60) + "…" : base;
            default -> "关于我：" + base + " 如果你也重视真诚沟通，我们应该会聊得来。";
        };
    }
}
