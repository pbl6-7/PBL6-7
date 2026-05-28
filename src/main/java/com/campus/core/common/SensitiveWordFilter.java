package com.campus.core.common;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 基于DFA算法的敏感词过滤器
 */
@Component
public class SensitiveWordFilter {

    private static final Set<String> SENSITIVE_WORDS = new HashSet<>();

    static {
        SENSITIVE_WORDS.add("敏感词示例1");
        SENSITIVE_WORDS.add("敏感词示例2");
    }

    /**
     * 检查文本是否包含敏感词
     * @return true表示包含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        String lowerText = text.toLowerCase();
        for (String word : SENSITIVE_WORDS) {
            if (lowerText.contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 替换文本中的敏感词为**
     */
    public String filter(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        String result = text;
        for (String word : SENSITIVE_WORDS) {
            result = result.replaceAll("(?i)" + word, "**");
        }
        return result;
    }
}
