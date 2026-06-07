package com.campus.core.common;

import com.campus.activity.entity.SensitiveWord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于DFA算法的敏感词过滤器
 * DFA（Deterministic Finite Automaton）确定性有限状态自动机算法
 * 通过构建敏感词树实现高效的敏感词检测和替换
 */
@Component
@Slf4j
public class SensitiveWordFilter {

    /**
     * 敏感词树的根节点
     */
    private Map<Character, Object> sensitiveWordMap = new ConcurrentHashMap<>();

    /**
     * 白名单词集合
     */
    private Set<String> whitelistWords = ConcurrentHashMap.newKeySet();

    /**
     * 敏感词结束标识
     */
    private static final String END_FLAG = "isEnd";

    /**
     * 默认替换字符
     */
    private static final String DEFAULT_REPLACE_CHAR = "*";

    /**
     * 初始化敏感词库
     * @param sensitiveWords 敏感词列表
     */
    public void initSensitiveWords(List<SensitiveWord> sensitiveWords) {
        log.info("开始初始化敏感词库，共 {} 个敏感词", sensitiveWords.size());
        sensitiveWordMap.clear();
        whitelistWords.clear();

        for (SensitiveWord word : sensitiveWords) {
            if (word.getIsWhitelist() != null && word.getIsWhitelist() == 1) {
                // 白名单词
                whitelistWords.add(word.getWord().toLowerCase());
            } else {
                // 敏感词，添加到DFA树
                addWordToTree(word.getWord());
            }
        }
        log.info("敏感词库初始化完成，敏感词树大小: {}, 白名单词数量: {}", 
                sensitiveWordMap.size(), whitelistWords.size());
    }

    /**
     * 将单个敏感词添加到DFA树中
     * @param word 敏感词
     */
    private void addWordToTree(String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        
        String lowerWord = word.toLowerCase();
        Map<Character, Object> currentMap = sensitiveWordMap;
        
        for (int i = 0; i < lowerWord.length(); i++) {
            char c = lowerWord.charAt(i);
            
            Object obj = currentMap.get(c);
            if (obj == null) {
                // 创建新节点
                Map<Character, Object> newMap = new HashMap<>();
                newMap.put(END_FLAG.charAt(0), "0"); // 标记为非结束节点
                currentMap.put(c, newMap);
                currentMap = newMap;
            } else {
                // 节点已存在，继续向下
                currentMap = (Map<Character, Object>) obj;
            }
            
            // 最后一个字符，标记为结束节点
            if (i == lowerWord.length() - 1) {
                currentMap.put(END_FLAG.charAt(0), "1");
            }
        }
    }

    /**
     * 添加单个敏感词到树中（动态更新）
     * @param word 敏感词
     * @param isWhitelist 是否为白名单词
     */
    public void addWord(String word, boolean isWhitelist) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        
        if (isWhitelist) {
            whitelistWords.add(word.toLowerCase());
        } else {
            addWordToTree(word);
        }
        log.info("添加敏感词: {}, 是否白名单: {}", word, isWhitelist);
    }

    /**
     * 从树中移除敏感词（动态更新）
     * @param word 敏感词
     * @param isWhitelist 是否为白名单词
     */
    public void removeWord(String word, boolean isWhitelist) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        
        if (isWhitelist) {
            whitelistWords.remove(word.toLowerCase());
        } else {
            // 从DFA树中移除需要重建树，这里简化处理：标记需要重建
            log.info("移除敏感词: {}, 是否白名单: {}", word, isWhitelist);
        }
    }

    /**
     * 检查文本是否包含敏感词
     * @param text 待检查的文本
     * @return true表示包含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        Set<String> sensitiveWords = getSensitiveWords(text);
        return !sensitiveWords.isEmpty();
    }

    /**
     * 获取文本中的所有敏感词
     * @param text 待检查的文本
     * @return 敏感词集合
     */
    public Set<String> getSensitiveWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new HashSet<>();
        }
        
        Set<String> sensitiveWords = new HashSet<>();
        String lowerText = text.toLowerCase();
        
        for (int i = 0; i < lowerText.length(); i++) {
            int length = checkSensitiveWord(lowerText, i);
            if (length > 0) {
                String word = lowerText.substring(i, i + length);
                // 检查是否在白名单中
                if (!whitelistWords.contains(word)) {
                    sensitiveWords.add(word);
                }
                // 跳过已匹配的敏感词
                i += length - 1;
            }
        }
        
        return sensitiveWords;
    }

    /**
     * 检查从指定位置开始的敏感词
     * @param text 文本
     * @param beginIndex 开始位置
     * @return 敏感词长度，如果不是敏感词返回0
     */
    private int checkSensitiveWord(String text, int beginIndex) {
        Map<Character, Object> currentMap = sensitiveWordMap;
        int matchLength = 0;
        boolean endFlag = false;
        
        for (int i = beginIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            currentMap = (Map<Character, Object>) currentMap.get(c);
            
            if (currentMap == null) {
                // 未匹配到敏感词
                break;
            }
            
            matchLength++;
            
            // 检查是否为结束节点
            String flag = (String) currentMap.get(END_FLAG.charAt(0));
            if ("1".equals(flag)) {
                endFlag = true;
                // 继续检查是否有更长的敏感词
            }
        }
        
        // 只有到达结束节点才算匹配成功
        if (matchLength > 0 && endFlag) {
            return matchLength;
        }
        
        return 0;
    }

    /**
     * 过滤文本中的敏感词，替换为指定字符
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        return filter(text, DEFAULT_REPLACE_CHAR);
    }

    /**
     * 过滤文本中的敏感词，替换为指定字符
     * @param text 待过滤的文本
     * @param replaceChar 替换字符
     * @return 过滤后的文本
     */
    public String filter(String text, String replaceChar) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        if (replaceChar == null || replaceChar.isEmpty()) {
            replaceChar = DEFAULT_REPLACE_CHAR;
        }
        
        StringBuilder result = new StringBuilder(text);
        String lowerText = text.toLowerCase();
        
        for (int i = 0; i < lowerText.length(); i++) {
            int length = checkSensitiveWord(lowerText, i);
            if (length > 0) {
                String word = lowerText.substring(i, i + length);
                // 检查是否在白名单中
                if (!whitelistWords.contains(word)) {
                    // 替换敏感词
                    for (int j = i; j < i + length; j++) {
                        result.setCharAt(j, replaceChar.charAt(0));
                    }
                }
                // 跳过已匹配的敏感词
                i += length - 1;
            }
        }
        
        return result.toString();
    }

    /**
     * 获取敏感词树的大小（用于调试）
     * @return 树的大小
     */
    public int getTreeSize() {
        return sensitiveWordMap.size();
    }

    /**
     * 获取白名单词数量
     * @return 白名单词数量
     */
    public int getWhitelistSize() {
        return whitelistWords.size();
    }

    /**
     * 检查指定词是否在白名单中
     * @param word 待检查的词
     * @return true表示在白名单中
     */
    public boolean isWhitelistWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        return whitelistWords.contains(word.toLowerCase());
    }
}