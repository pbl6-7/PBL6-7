package com.campus.core.common;

import com.campus.activity.entity.SensitiveWord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 敏感词过滤器单元测试
 */
class SensitiveWordFilterTest {

    private SensitiveWordFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SensitiveWordFilter();
        // 初始化测试敏感词
        List<SensitiveWord> words = new ArrayList<>();

        SensitiveWord w1 = new SensitiveWord();
        w1.setWord("暴力");
        w1.setIsWhitelist(0);
        w1.setType("violence");
        words.add(w1);

        SensitiveWord w2 = new SensitiveWord();
        w2.setWord("色情");
        w2.setIsWhitelist(0);
        w2.setType("porn");
        words.add(w2);

        SensitiveWord w3 = new SensitiveWord();
        w3.setWord("赌博");
        w3.setIsWhitelist(0);
        w3.setType("other");
        words.add(w3);

        SensitiveWord w4 = new SensitiveWord();
        w4.setWord("毒品");
        w4.setIsWhitelist(0);
        w4.setType("other");
        words.add(w4);

        // 白名单词
        SensitiveWord w5 = new SensitiveWord();
        w5.setWord("禁毒");
        w5.setIsWhitelist(1);
        w5.setType("other");
        words.add(w5);

        filter.initSensitiveWords(words);
    }

    @Test
    void testContainsSensitiveWord_Found() {
        assertTrue(filter.containsSensitiveWord("这里包含暴力内容"));
        assertTrue(filter.containsSensitiveWord("色情信息"));
        assertTrue(filter.containsSensitiveWord("参与赌博活动"));
        assertTrue(filter.containsSensitiveWord("贩卖毒品"));
    }

    @Test
    void testContainsSensitiveWord_NotFound() {
        assertFalse(filter.containsSensitiveWord("今天天气真好"));
        assertFalse(filter.containsSensitiveWord("校园活动发布"));
        assertFalse(filter.containsSensitiveWord("篮球比赛报名"));
    }

    @Test
    void testFilter_ReplacesSensitiveWords() {
        String result = filter.filter("这里包含暴力内容");
        assertFalse(result.contains("暴力"));
        assertTrue(result.contains("**"));
    }

    @Test
    void testFilter_NoSensitiveWords() {
        String input = "今天天气真好";
        String result = filter.filter(input);
        assertEquals(input, result);
    }

    @Test
    void testAddWord_Dynamically() {
        // 添加新敏感词
        filter.addWord("诈骗", false);
        assertTrue(filter.containsSensitiveWord("网络诈骗"));
    }

    @Test
    void testRemoveWord() {
        // 移除敏感词
        filter.removeWord("暴力", false);
        // 移除后不应再检测到
        assertFalse(filter.containsSensitiveWord("暴力"));
    }

    @Test
    void testWhitelist() {
        // "禁毒" 是白名单词，包含"毒"但"禁毒"本身不应被过滤
        // 注意：白名单只影响完整匹配"禁毒"这个词
        String result = filter.filter("参与禁毒宣传");
        // "禁毒"在白名单中，不应被替换
        assertTrue(result.contains("禁毒"));
    }

    @Test
    void testEmptyInput() {
        assertFalse(filter.containsSensitiveWord(""));
        assertFalse(filter.containsSensitiveWord(null));
        assertEquals("", filter.filter(""));
    }

    @Test
    void testGetTreeSize() {
        int size = filter.getTreeSize();
        assertTrue(size > 0);
    }
}
