package com.campus.activity.service;

import com.campus.activity.dto.ActivityPageResponse;
import com.campus.activity.dto.ActivityQueryRequest;
import com.campus.activity.dto.SearchSuggestionResponse;
import com.campus.activity.mapper.SearchHistoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索服务类
 * 
 * 提供搜索相关的业务逻辑，包括：
 * - 搜索建议缓存
 * - 热门搜索词缓存
 * - 搜索历史记录
 * - 搜索性能监控
 * - 智能搜索建议算法
 */
@Slf4j
@Service
public class SearchService {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private SearchHistoryService searchHistoryService;

    @Autowired
    private SearchHistoryMapper searchHistoryMapper;

    @Autowired
    private SearchPerformanceMonitor performanceMonitor;

    @Autowired
    private ActivityService activityService;

    /**
     * 搜索建议结果类
     */
    public static class SearchSuggestion {
        private String keyword;
        private int relevanceScore;
        private int searchCount;
        private String suggestionType;

        public SearchSuggestion(String keyword, int relevanceScore, int searchCount, String suggestionType) {
            this.keyword = keyword;
            this.relevanceScore = relevanceScore;
            this.searchCount = searchCount;
            this.suggestionType = suggestionType;
        }

        public String getKeyword() { return keyword; }
        public int getRelevanceScore() { return relevanceScore; }
        public int getSearchCount() { return searchCount; }
        public String getSuggestionType() { return suggestionType; }
    }

    /**
     * 搜索请求参数类
     */
    public static class SearchRequest {
        private String keyword;
        private int page = 1;
        private int pageSize = 10;
        private String sortBy = "relevance";
        private String sortOrder = "desc";
        private String searchType = "activity";

        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }
        public String getSortOrder() { return sortOrder; }
        public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }
        public String getSearchType() { return searchType; }
        public void setSearchType(String searchType) { this.searchType = searchType; }
    }

    /**
     * 搜索结果类
     */
    public static class SearchResult<T> {
        private List<T> items;
        private int totalCount;
        private int page;
        private int pageSize;
        private boolean cacheHit;
        private long searchTimeMs;

        public SearchResult(List<T> items, int totalCount, int page, int pageSize) {
            this.items = items;
            this.totalCount = totalCount;
            this.page = page;
            this.pageSize = pageSize;
        }

        public List<T> getItems() { return items; }
        public int getTotalCount() { return totalCount; }
        public int getPage() { return page; }
        public int getPageSize() { return pageSize; }
        public boolean isCacheHit() { return cacheHit; }
        public void setCacheHit(boolean cacheHit) { this.cacheHit = cacheHit; }
        public long getSearchTimeMs() { return searchTimeMs; }
        public void setSearchTimeMs(long searchTimeMs) { this.searchTimeMs = searchTimeMs; }
    }

    /**
     * 执行搜索（带缓存、历史记录和性能监控）
     * 
     * @param userId 用户ID
     * @param request 搜索请求参数
     * @param searchFunction 实际搜索函数
     * @return 搜索结果
     */
    public <T> SearchResult<T> search(Long userId, SearchRequest request, 
                                       SearchFunction<T> searchFunction) {
        long startTime = System.currentTimeMillis();
        boolean cacheHit = false;
        boolean success = false;
        int resultCount = 0;

        try {
            // 生成缓存键
            String cacheKey = generateCacheKey(request);

            // 尝试从缓存获取
            SearchResult<T> cachedResult = cacheService.getSearchSuggestion(cacheKey, SearchResult.class, () -> { });
            
            if (cachedResult != null) {
                cacheHit = true;
                cachedResult.setCacheHit(true);
                resultCount = cachedResult.getTotalCount();
                success = true;
                
                // 记录性能数据
                long timeMs = System.currentTimeMillis() - startTime;
                performanceMonitor.recordSearch(request.getKeyword(), timeMs, resultCount, success, cacheHit, request.getSearchType());
                
                log.debug("搜索缓存命中: keyword={}, cacheKey={}, resultCount={}", 
                        request.getKeyword(), cacheKey, resultCount);
                return cachedResult;
            }

            // 执行实际搜索
            SearchResult<T> result = searchFunction.search(request);
            resultCount = result.getTotalCount();
            success = true;

            // 缓存搜索结果（热门搜索结果缓存）
            if (resultCount > 0 && request.getPage() == 1) {
                cacheService.putSearchSuggestion(cacheKey, result);
                log.debug("搜索结果已缓存: keyword={}, cacheKey={}, resultCount={}", 
                        request.getKeyword(), cacheKey, resultCount);
            }

            // 记录搜索历史（异步执行）
            searchHistoryService.recordSearchHistory(userId, request.getKeyword(), String.valueOf(resultCount), request.getSearchType());

            // 记录性能数据
            long timeMs = System.currentTimeMillis() - startTime;
            result.setSearchTimeMs(timeMs);
            performanceMonitor.recordSearch(request.getKeyword(), timeMs, resultCount, success, cacheHit, request.getSearchType());

            log.info("搜索完成: keyword={}, resultCount={}, timeMs={}, cacheHit={}", 
                    request.getKeyword(), resultCount, timeMs, cacheHit);

            return result;
        } catch (Exception e) {
            success = false;
            long timeMs = System.currentTimeMillis() - startTime;
            performanceMonitor.recordSearch(request.getKeyword(), timeMs, resultCount, success, cacheHit, request.getSearchType());
            
            log.error("搜索失败: keyword={}, error={}", request.getKeyword(), e.getMessage());
            throw e;
        }
    }

    /**
     * 搜索函数接口
     */
    @FunctionalInterface
    public interface SearchFunction<T> {
        SearchResult<T> search(SearchRequest request);
    }

    /**
     * 获取搜索建议（带缓存，优化算法）
     * 
     * 支持多种匹配方式：
     * - 前缀匹配
     * - 模糊匹配
     * - 基于历史记录的智能建议
     * 
     * @param keyword 搜索关键词
     * @param limit 限制数量
     * @return 搜索建议列表
     */
    public List<SearchSuggestion> getSearchSuggestions(String keyword, int limit) {
        String cacheKey = String.format("searchSuggestion:keyword:%s:%d", keyword, limit);

        try {
            // 先从缓存获取
            List<SearchSuggestion> cached = cacheService.getSearchSuggestion(cacheKey, List.class, null);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("搜索建议缓存读取失败: keyword={}, error={}", keyword, e.getMessage());
        }

        // 缓存未命中，从数据库获取
        List<SearchSuggestion> suggestions = generateSmartSuggestions(keyword, limit);

        // 写入缓存
        try {
            cacheService.putSearchSuggestion(cacheKey, suggestions);
        } catch (Exception e) {
            log.warn("搜索建议缓存写入失败: keyword={}, error={}", keyword, e.getMessage());
        }

        return suggestions;
    }

    /**
     * 生成智能搜索建议
     * 
     * 优化算法：
     * 1. 前缀匹配（精确匹配优先）
     * 2. 包含匹配（模糊匹配）
     * 3. 历史记录匹配（基于用户搜索习惯）
     * 4. 按相关性排序（匹配度 + 搜索次数）
     * 
     * @param keyword 关键词
     * @param limit 限制数量
     * @return 搜索建议列表
     */
    private List<SearchSuggestion> generateSmartSuggestions(String keyword, int limit) {
        List<SearchSuggestion> suggestions = new ArrayList<>();

        // 1. 从历史记录获取前缀匹配的建议
        List<String> prefixMatches = searchHistoryMapper.selectSuggestionsByPrefix(keyword, limit * 2);
        for (int i = 0; i < prefixMatches.size(); i++) {
            String match = prefixMatches.get(i);
            int relevanceScore = 100 - i; // 前缀匹配得分（越靠前得分越高）
            long searchCount = searchHistoryMapper.countByKeyword(match);
            suggestions.add(new SearchSuggestion(match, relevanceScore, (int) searchCount, "prefix"));
        }

        // 2. 添加包含匹配的建议（模糊匹配）
        List<String> fuzzyMatches = generateFuzzyMatches(keyword, limit);
        for (int i = 0; i < fuzzyMatches.size(); i++) {
            String match = fuzzyMatches.get(i);
            int relevanceScore = 50 - i; // 模糊匹配得分较低
            long searchCount = searchHistoryMapper.countByKeyword(match);
            suggestions.add(new SearchSuggestion(match, relevanceScore, (int) searchCount, "fuzzy"));
        }

        // 3. 添加热门搜索词作为补充建议
        List<String> hotKeywords = searchHistoryService.getHotKeywords(limit, 7);
        for (int i = 0; i < hotKeywords.size(); i++) {
            String hotKeyword = hotKeywords.get(i);
            if (!keyword.isEmpty() && hotKeyword.toLowerCase().contains(keyword.toLowerCase())) {
                int relevanceScore = 30 - i; // 热门词匹配得分
                long searchCount = searchHistoryMapper.countByKeyword(hotKeyword);
                suggestions.add(new SearchSuggestion(hotKeyword, relevanceScore, (int) searchCount, "hot"));
            }
        }

        // 4. 按相关性排序（相关性得分 + 搜索次数权重）
        suggestions.sort((a, b) -> {
            // 综合得分 = 相关性得分 + 搜索次数权重（搜索次数越高权重越大）
            int scoreA = a.getRelevanceScore() + Math.min(a.getSearchCount() / 10, 20);
            int scoreB = b.getRelevanceScore() + Math.min(b.getSearchCount() / 10, 20);
            return Integer.compare(scoreB, scoreA);
        });

        // 5. 去重并限制数量
        Set<String> uniqueKeywords = new HashSet<>();
        List<SearchSuggestion> result = new ArrayList<>();
        for (SearchSuggestion suggestion : suggestions) {
            if (!uniqueKeywords.contains(suggestion.getKeyword())) {
                uniqueKeywords.add(suggestion.getKeyword());
                result.add(suggestion);
                if (result.size() >= limit) {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 生成模糊匹配建议
     * 
     * @param keyword 关键词
     * @param limit 限制数量
     * @return 模糊匹配建议列表
     */
    private List<String> generateFuzzyMatches(String keyword, int limit) {
        List<String> fuzzyMatches = new ArrayList<>();

        // 基于关键词生成相关建议
        if (keyword.length() >= 2) {
            // 添加常见后缀
            fuzzyMatches.add(keyword + "活动");
            fuzzyMatches.add(keyword + "讲座");
            fuzzyMatches.add(keyword + "比赛");
            fuzzyMatches.add(keyword + "社团");
            fuzzyMatches.add(keyword + "培训");
            fuzzyMatches.add(keyword + "展览");
            fuzzyMatches.add(keyword + "演出");
            fuzzyMatches.add(keyword + "志愿服务");
        }

        return fuzzyMatches.subList(0, Math.min(limit, fuzzyMatches.size()));
    }

    /**
     * 获取热门搜索词（带缓存）
     * 
     * @param limit 限制数量
     * @return 热门搜索词列表
     */
    public List<String> getHotKeywords(int limit) {
        return searchHistoryService.getHotKeywords(limit, 7);
    }

    /**
     * 获取用户搜索建议（基于用户历史）
     * 
     * @param userId 用户ID
     * @param keyword 关键词
     * @param limit 限制数量
     * @return 搜索建议列表
     */
    public List<SearchSuggestion> getUserSearchSuggestions(Long userId, String keyword, int limit) {
        String cacheKey = String.format("searchSuggestion:user:%d:%s:%d", userId, keyword, limit);

        try {
            return cacheService.getSearchSuggestion(cacheKey, List.class, () -> {
                // 获取用户最近搜索的关键词
                List<String> userKeywords = searchHistoryService.getUserRecentKeywords(userId, limit * 2);
                
                List<SearchSuggestion> suggestions = new ArrayList<>();
                
                // 匹配用户历史关键词
                for (int i = 0; i < userKeywords.size(); i++) {
                    String userKeyword = userKeywords.get(i);
                    if (userKeyword.toLowerCase().contains(keyword.toLowerCase())) {
                        int relevanceScore = 100 - i;
                        long searchCount = searchHistoryMapper.countByKeyword(userKeyword);
                        suggestions.add(new SearchSuggestion(userKeyword, relevanceScore, (int) searchCount, "user_history"));
                    }
                }
                
                // 补充通用建议
                if (suggestions.size() < limit) {
                    List<SearchSuggestion> generalSuggestions = getSearchSuggestions(keyword, limit - suggestions.size());
                    suggestions.addAll(generalSuggestions);
                }
                
                // 排序并限制数量
                suggestions.sort((a, b) -> Integer.compare(b.getRelevanceScore(), a.getRelevanceScore()));
                suggestions = new ArrayList<>(suggestions.subList(0, Math.min(limit, suggestions.size())));
            });
        } catch (Exception e) {
            // 缓存加载失败时，返回空列表
            log.warn("用户搜索建议缓存加载失败: userId={}, keyword={}, error={}", userId, keyword, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 记录搜索行为（清除相关缓存）
     * 
     * @param keyword 搜索关键词
     */
    public void recordSearch(String keyword) {
        log.info("记录搜索行为：keyword={}", keyword);
        
        // 清除热门搜索词缓存，以便重新统计
        cacheService.evictSearchSuggestion("searchSuggestion:hotKeywords:*");
        log.info("已清除热门搜索词缓存");
    }

    /**
     * 更新搜索建议（清除相关缓存）
     * 
     * @param keyword 关键词
     */
    public void updateSearchSuggestions(String keyword) {
        log.info("更新搜索建议：keyword={}", keyword);
        
        // 清除该关键词的搜索建议缓存
        cacheService.evictSearchSuggestion("searchSuggestion:keyword:" + keyword + ":*");
        log.info("已清除搜索建议缓存：keyword={}", keyword);
    }

    /**
     * 清除所有搜索缓存
     */
    public void clearAllSearchCache() {
        cacheService.clearSearchSuggestionCache();
        log.info("已清除所有搜索建议缓存");
    }

    /**
     * 生成缓存键
     * 
     * @param request 搜索请求
     * @return 缓存键
     */
    private String generateCacheKey(SearchRequest request) {
        return String.format("searchResult:%s:%s:%d:%d:%s:%s",
                request.getSearchType(),
                request.getKeyword(),
                request.getPage(),
                request.getPageSize(),
                request.getSortBy(),
                request.getSortOrder());
    }

    /**
     * 获取搜索性能统计
     * 
     * @return 性能统计数据
     */
    public SearchPerformanceMonitor.GlobalStats getPerformanceStats() {
        return performanceMonitor.getGlobalStats();
    }

    /**
     * 获取搜索性能摘要报告
     * 
     * @return 性能摘要报告
     */
    public SearchPerformanceMonitor.PerformanceSummary getPerformanceSummary() {
        return performanceMonitor.getPerformanceSummary();
    }

    /**
     * 获取慢搜索记录
     * 
     * @param thresholdMs 阈值（毫秒）
     * @return 慢搜索记录列表
     */
    public List<SearchPerformanceMonitor.SearchRecord> getSlowSearches(long thresholdMs) {
        return performanceMonitor.getSlowSearches(thresholdMs);
    }

    /**
     * 清除性能统计数据
     */
    public void clearPerformanceStats() {
        performanceMonitor.clearStats();
    }

    // ==================== 适配现有Controller的方法 ====================

    /**
     * 获取搜索建议和热门搜索（适配SearchController）
     * 
     * @param prefix 搜索前缀
     * @return 搜索建议响应
     */
    public SearchSuggestionResponse getSearchSuggestions(String prefix) {
        List<String> suggestions;
        if (prefix != null && !prefix.trim().isEmpty()) {
            List<SearchSuggestion> suggestionList = getSearchSuggestions(prefix.trim(), 10);
            suggestions = suggestionList.stream()
                    .map(SearchSuggestion::getKeyword)
                    .collect(Collectors.toList());
        } else {
            suggestions = new ArrayList<>();
        }
        
        List<String> hotSearches = getHotSearches();
        
        return new SearchSuggestionResponse(suggestions, hotSearches);
    }

    /**
     * 搜索自动补全（适配SearchController）
     * 
     * @param prefix 搜索前缀
     * @return 自动补全建议列表
     */
    public List<String> autocomplete(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return getHotSearches();
        }
        
        List<SearchSuggestion> suggestions = getSearchSuggestions(prefix.trim(), 10);
        return suggestions.stream()
                .map(SearchSuggestion::getKeyword)
                .collect(Collectors.toList());
    }

    /**
     * 获取热门搜索（适配SearchController）
     *
     * @return 热门搜索列表
     */
    public List<String> getHotSearches() {
        return getHotKeywords(10);
    }

    /**
     * 搜索活动（适配SearchController）
     *
     * @param queryRequest 活动查询请求
     * @param userId 用户ID
     * @return 活动分页响应
     */
    public ActivityPageResponse searchActivities(ActivityQueryRequest queryRequest, Long userId) {
        log.info("执行活动搜索: keyword={}, type={}, status={}, page={}, size={}",
                queryRequest.getKeyword(), queryRequest.getType(), queryRequest.getStatus(),
                queryRequest.getPage(), queryRequest.getSize());
        return activityService.getActivityList(userId, queryRequest);
    }
}