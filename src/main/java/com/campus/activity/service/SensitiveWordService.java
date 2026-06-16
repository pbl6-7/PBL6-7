package com.campus.activity.service;

import com.campus.activity.entity.SensitiveWord;
import com.campus.activity.mapper.SensitiveWordMapper;
import com.campus.core.common.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 敏感词服务类
 * 提供敏感词的CRUD操作和DFA树初始化
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;
    private final SensitiveWordFilter sensitiveWordFilter;

    /**
     * 应用启动时自动初始化敏感词DFA树
     */
    @PostConstruct
    public void init() {
        refreshSensitiveWordTree();
    }

    /**
     * 刷新敏感词DFA树（从数据库重新加载）
     */
    public void refreshSensitiveWordTree() {
        List<SensitiveWord> words = sensitiveWordMapper.selectAll();
        sensitiveWordFilter.initSensitiveWords(words);
        log.info("敏感词DFA树刷新完成，共加载{}个敏感词", words.size());
    }

    /**
     * 获取所有敏感词
     */
    public List<SensitiveWord> getAllSensitiveWords() {
        return sensitiveWordMapper.selectAll();
    }

    /**
     * 根据类型获取敏感词
     */
    public List<SensitiveWord> getSensitiveWordsByType(String type) {
        return sensitiveWordMapper.selectByType(type);
    }

    /**
     * 根据ID获取敏感词
     */
    public SensitiveWord getSensitiveWordById(Long id) {
        return sensitiveWordMapper.selectById(id);
    }

    /**
     * 创建敏感词
     */
    @Transactional(rollbackFor = Exception.class)
    public SensitiveWord createSensitiveWord(SensitiveWord sensitiveWord) {
        // 校验敏感词内容
        if (sensitiveWord.getWord() == null || sensitiveWord.getWord().trim().isEmpty()) {
            throw new IllegalArgumentException("敏感词内容不能为空");
        }
        // 设置默认值
        if (sensitiveWord.getIsWhitelist() == null) {
            sensitiveWord.setIsWhitelist(0);
        }
        sensitiveWordMapper.insert(sensitiveWord);
        // 动态添加到DFA树
        boolean isWhitelist = sensitiveWord.getIsWhitelist() != null && sensitiveWord.getIsWhitelist() == 1;
        sensitiveWordFilter.addWord(sensitiveWord.getWord(), isWhitelist);
        log.info("创建敏感词: id={}, word={}", sensitiveWord.getId(), sensitiveWord.getWord());
        return sensitiveWord;
    }

    /**
     * 更新敏感词
     */
    @Transactional(rollbackFor = Exception.class)
    public SensitiveWord updateSensitiveWord(Long id, SensitiveWord sensitiveWord) {
        SensitiveWord existing = sensitiveWordMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("敏感词不存在");
        }
        sensitiveWord.setId(id);
        sensitiveWordMapper.updateById(sensitiveWord);
        // 重建DFA树
        refreshSensitiveWordTree();
        log.info("更新敏感词: id={}", id);
        return sensitiveWordMapper.selectById(id);
    }

    /**
     * 删除敏感词
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSensitiveWord(Long id) {
        SensitiveWord existing = sensitiveWordMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("敏感词不存在");
        }
        sensitiveWordMapper.deleteById(id);
        // 重建DFA树
        refreshSensitiveWordTree();
        log.info("删除敏感词: id={}", id);
    }

    /**
     * 批量删除敏感词
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteSensitiveWords(List<Long> ids) {
        sensitiveWordMapper.batchDelete(ids);
        refreshSensitiveWordTree();
        log.info("批量删除敏感词: ids={}", ids);
    }

    /**
     * 获取敏感词统计
     */
    public java.util.Map<String, Object> getSensitiveWordStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total", sensitiveWordMapper.countAll());
        stats.put("politics", sensitiveWordMapper.countByType("politics"));
        stats.put("violence", sensitiveWordMapper.countByType("violence"));
        stats.put("porn", sensitiveWordMapper.countByType("porn"));
        stats.put("other", sensitiveWordMapper.countByType("other"));
        stats.put("treeSize", sensitiveWordFilter.getTreeSize());
        stats.put("whitelistSize", sensitiveWordFilter.getWhitelistSize());
        return stats;
    }

    /**
     * 检查文本是否包含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        return sensitiveWordFilter.containsSensitiveWord(text);
    }

    /**
     * 过滤文本中的敏感词
     */
    public String filterSensitiveWords(String text) {
        return sensitiveWordFilter.filter(text);
    }
}
