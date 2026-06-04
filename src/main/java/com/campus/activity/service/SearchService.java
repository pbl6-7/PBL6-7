package com.campus.activity.service;

import com.campus.activity.dto.SearchSuggestionResponse;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.entity.Activity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ActivityMapper activityMapper;

    private static final int MAX_SEARCH_PREFIX_LENGTH = 50;
    private static final int MAX_SUGGESTIONS = 10;
    private static final int SEARCH_RESULT_LIMIT = 50;

    private static final List<String> DEFAULT_HOT_SEARCHES = Arrays.asList(
            "学术讲座", "体育比赛", "文艺演出", "志愿服务", "社会实践",
            "科技创新", "校园招聘", "新生晚会", "毕业典礼", "运动会"
    );

    /**
     * 获取搜索建议和热门搜索
     * @param prefix 用户输入的搜索前缀
     * @return 搜索建议和热门搜索
     */
    public SearchSuggestionResponse getSearchSuggestions(String prefix) {
        List<String> suggestions = new ArrayList<>();
        List<String> hotSearches = getHotSearches();

        if (prefix != null && !prefix.trim().isEmpty()) {
            String sanitizedPrefix = sanitizeAndValidatePrefix(prefix);
            if (sanitizedPrefix != null) {
                suggestions = getSuggestionsByPrefix(sanitizedPrefix);
            }
        }

        return new SearchSuggestionResponse(suggestions, hotSearches);
    }

    /**
     * 清理并验证搜索前缀
     * 修复问题11：加强输入验证，防止SQL注入和XSS
     * @param prefix 原始前缀
     * @return 清理后的前缀，如果无效返回null
     */
    private String sanitizeAndValidatePrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return null;
        }
        String trimmed = prefix.trim();
        if (trimmed.length() > MAX_SEARCH_PREFIX_LENGTH) {
            trimmed = trimmed.substring(0, MAX_SEARCH_PREFIX_LENGTH);
        }

        // 修复：移除可能导致SQL注入或XSS的特殊字符
        trimmed = trimmed.replaceAll("[<>'\";\\\\/&%$#@!~`]", "");
        
        // 修复：只允许中英文、数字、空格和常见标点
        if (!trimmed.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9\\s\\-_.，。、]+$")) {
            return null;
        }
        
        return trimmed;
    }

    /**
     * 根据前缀获取搜索建议
     * @param prefix 搜索前缀
     * @return 建议列表
     */
    private List<String> getSuggestionsByPrefix(String prefix) {
        List<Activity> activities = activityMapper.selectList(
                null,
                prefix,
                null,
                null,
                null,
                null,
                null,
                null,
                "createdAt",
                "desc",
                Integer.valueOf(0),
                Integer.valueOf(50)
        );

        Set<String> suggestionSet = new LinkedHashSet<>();

        for (Activity activity : activities) {
            if (activity.getTitle() != null && activity.getTitle().toLowerCase().contains(prefix.toLowerCase())) {
                suggestionSet.add(activity.getTitle());
            }
            if (activity.getDescription() != null && activity.getDescription().toLowerCase().contains(prefix.toLowerCase())) {
                String[] words = activity.getDescription().split("[,，。、\\s]+");
                for (String word : words) {
                    if (word.toLowerCase().startsWith(prefix.toLowerCase()) && word.length() > prefix.length()) {
                        suggestionSet.add(word);
                    }
                }
            }
            if (activity.getLocation() != null && activity.getLocation().toLowerCase().contains(prefix.toLowerCase())) {
                suggestionSet.add(activity.getLocation());
            }
        }

        return suggestionSet.stream().limit(10).collect(Collectors.toList());
    }

    /**
     * 获取热门搜索
     * @return 热门搜索列表
     */
    public List<String> getHotSearches() {
        return new ArrayList<>(DEFAULT_HOT_SEARCHES);
    }

    /**
     * 搜索建议补全
     * @param prefix 用户输入
     * @return 补全建议列表
     */
    public List<String> autocomplete(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return getHotSearches();
        }
        String sanitizedPrefix = sanitizeAndValidatePrefix(prefix);
        if (sanitizedPrefix == null) {
            return getHotSearches();
        }
        return getSuggestionsByPrefix(sanitizedPrefix);
    }
}
