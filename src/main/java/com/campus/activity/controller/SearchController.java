package com.campus.activity.controller;

import com.campus.activity.dto.SearchSuggestionResponse;
import com.campus.activity.entity.SearchHistory;
import com.campus.activity.service.SearchHistoryService;
import com.campus.activity.service.SearchService;
import com.campus.core.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 智能搜索控制器
 *
 * 提供搜索建议、自动补全、热门搜索以及搜索历史管理接口
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Api(tags = "智能搜索")
public class SearchController {

    private final SearchService searchService;
    private final SearchHistoryService searchHistoryService;

    @GetMapping("/suggestions")
    @ApiOperation("获取搜索建议和热门搜索")
    public Result<SearchSuggestionResponse> getSearchSuggestions(
            @ApiParam(value = "搜索前缀", required = false)
            @RequestParam(value = "prefix", required = false) String prefix) {
        SearchSuggestionResponse response = searchService.getSearchSuggestions(prefix);
        return Result.success(response);
    }

    @GetMapping("/autocomplete")
    @ApiOperation("搜索自动补全")
    public Result<List<String>> autocomplete(
            @ApiParam(value = "搜索前缀", required = false)
            @RequestParam(value = "prefix", required = false) String prefix) {
        List<String> suggestions = searchService.autocomplete(prefix);
        return Result.success(suggestions);
    }

    @GetMapping("/hot")
    @ApiOperation("获取热门搜索")
    public Result<List<String>> getHotSearches() {
        List<String> hotSearches = searchService.getHotSearches();
        return Result.success(hotSearches);
    }

    /**
     * 清除当前用户所有搜索历史
     *
     * @param request HTTP请求对象，用于获取当前用户ID
     * @return 删除的记录数
     */
    @DeleteMapping("/history")
    @ApiOperation("清除当前用户所有搜索历史")
    public Result<Integer> clearSearchHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        int deleted = searchHistoryService.clearAllUserSearchHistory(userId);
        return Result.success(deleted, "搜索历史已清除");
    }

    /**
     * 获取当前用户搜索历史
     *
     * @param request HTTP请求对象，用于获取当前用户ID
     * @return 搜索历史列表
     */
    @GetMapping("/history")
    @ApiOperation("获取当前用户搜索历史")
    public Result<List<SearchHistory>> getSearchHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        List<SearchHistory> histories = searchHistoryService.getUserSearchHistory(userId, 20);
        return Result.success(histories);
    }
}
