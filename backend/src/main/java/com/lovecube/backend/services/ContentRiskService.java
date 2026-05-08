package com.lovecube.backend.services;

import com.lovecube.backend.entity.ContentRiskLog;
import com.lovecube.backend.repository.ContentRiskLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContentRiskService {

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Autowired
    private SemanticNormalizeService semanticNormalizeService;

    @Autowired
    private ContentRiskLogRepository logRepository;

    /**
     * 检测文本风险，命中时记录审计日志，返回检测结果 Map。
     *
     * @param text    待检测文本
     * @param userId  发起请求的用户 ID，未登录时为 null
     * @param context 调用场景标识，如 "group-post"、"comment"、"group-info"
     * @return Map 包含 riskLevel, hitWords, suggestion, allowPublish
     */
    public Map<String, Object> check(String text, Long userId, String context) {
        List<String> hits = sensitiveWordService.detectHits(text);
        String riskLevel = sensitiveWordService.calculateRiskLevel(hits.size());
        String suggestion = hits.isEmpty() ? "" : semanticNormalizeService.normalize(text);

        if (!hits.isEmpty()) {
            ContentRiskLog log = new ContentRiskLog();
            log.setOriginalText(text);
            log.setSuggestedText(suggestion);
            log.setHitWords(String.join(",", hits));
            log.setRiskLevel(riskLevel);
            log.setUserId(userId);
            log.setContext(context);
            logRepository.save(log);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("riskLevel",    riskLevel);
        result.put("hitWords",     hits);
        result.put("suggestion",   suggestion);
        result.put("allowPublish", true);
        return result;
    }
}
