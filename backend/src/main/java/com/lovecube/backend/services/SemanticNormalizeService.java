package com.lovecube.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class SemanticNormalizeService {

    @Autowired
    private SensitiveWordService sensitiveWordService;

    /**
     * 将文本中所有敏感词替换为中性表达，返回建议文案。
     * 若无命中词则返回原文。
     */
    public String normalize(String text) {
        if (text == null || text.isBlank()) return text;
        String result = text;
        for (Map.Entry<String, String> entry : sensitiveWordService.getWordMap().entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
