package com.campus.activity.service;

import com.campus.activity.entity.SearchHistory;
import com.campus.activity.mapper.SearchHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索历史服务
 */
@Slf4j
@Service
public class SearchHistoryService {

    @Autowired
    private SearchHistoryMapper searchHistoryMapper;

    /**
     * 记录搜索
     */
    public void recordSearch(Long userId, String keyword, String searchType, String searchResult) {
        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setSearchKeyword(keyword);
        history.setSearchType(searchType);
        history.setSearchTime(LocalDateTime.now());
        searchHistoryMapper.insert(history);
    }

    /**
     * 记录搜索历史
     */
    public void recordSearchHistory(Long userId, String keyword, String searchType, String searchResult) {
        recordSearch(userId, keyword, searchType, searchResult);
    }

    /**
     * 获取用户搜索历史
     */
    public List<String> getUserRecentKeywords(Long userId, int limit) {
        List<SearchHistory> histories = searchHistoryMapper.selectByUserId(userId, limit);
        return histories.stream()
                .map(SearchHistory::getSearchKeyword)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取用户搜索历史（完整版本）
     */
    public List<SearchHistory> getUserSearchHistory(Long userId, int limit) {
        return searchHistoryMapper.selectByUserId(userId, limit);
    }

    /**
     * 清除用户所有搜索历史
     */
    public int clearAllUserSearchHistory(Long userId) {
        searchHistoryMapper.deleteByUserId(userId);
        return 1;
    }

    /**
     * 获取热门关键词
     */
    public List<String> getHotKeywords(int days, int limit) {
        try {
            List<SearchHistory> recentSearches = searchHistoryMapper.selectByUserId(null, limit * 10);
            return recentSearches.stream()
                    .filter(h -> h.getSearchTime() != null &&
                            h.getSearchTime().isAfter(LocalDateTime.now().minusDays(days)))
                    .map(SearchHistory::getSearchKeyword)
                    .filter(k -> k != null && !k.trim().isEmpty())
                    .collect(Collectors.groupingBy(k -> k, Collectors.counting()))
                    .entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("获取热门关键词失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 清除用户搜索历史
     */
    public void clearUserHistory(Long userId) {
        searchHistoryMapper.deleteByUserId(userId);
    }
}
