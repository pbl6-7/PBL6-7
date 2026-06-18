package com.campus.activity.service;

import com.campus.BaseIntegrationTest;
import com.campus.activity.entity.SensitiveWord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 敏感词服务集成测试
 */
class SensitiveWordServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Test
    void testCreateSensitiveWord() {
        SensitiveWord word = new SensitiveWord();
        word.setWord("测试敏感词_" + System.currentTimeMillis());
        word.setIsWhitelist(0);
        word.setType("other");

        SensitiveWord created = sensitiveWordService.createSensitiveWord(word);
        assertNotNull(created);
        assertNotNull(created.getId());
    }

    @Test
    void testGetAllSensitiveWords() {
        List<SensitiveWord> words = sensitiveWordService.getAllSensitiveWords();
        assertNotNull(words);
    }

    @Test
    void testContainsAndFilter() {
        SensitiveWord word = new SensitiveWord();
        word.setWord("过滤测试词_" + System.currentTimeMillis());
        word.setIsWhitelist(0);
        word.setType("other");
        sensitiveWordService.createSensitiveWord(word);

        assertTrue(sensitiveWordService.containsSensitiveWord(word.getWord()));

        String result = sensitiveWordService.filterSensitiveWords(
                "这里包含" + word.getWord() + "的内容");
        assertFalse(result.contains(word.getWord()));
    }

    @Test
    void testDeleteSensitiveWord() {
        SensitiveWord word = new SensitiveWord();
        word.setWord("待删除词_" + System.currentTimeMillis());
        word.setIsWhitelist(0);
        word.setType("other");
        SensitiveWord created = sensitiveWordService.createSensitiveWord(word);

        assertDoesNotThrow(() -> sensitiveWordService.deleteSensitiveWord(created.getId()));
    }

    @Test
    void testGetSensitiveWordStats() {
        Map<String, Object> stats = sensitiveWordService.getSensitiveWordStats();
        assertNotNull(stats);
        assertTrue(stats.containsKey("total"));
    }

    @Test
    void testRefreshSensitiveWordTree() {
        assertDoesNotThrow(() -> sensitiveWordService.refreshSensitiveWordTree());
    }
}
