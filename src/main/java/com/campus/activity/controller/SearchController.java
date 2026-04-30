package com.campus.activity.controller;

import com.campus.activity.dto.SearchSuggestionResponse;
import com.campus.activity.service.SearchService;
import com.campus.core.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Api(tags = "智能搜索")
public class SearchController {

    private final SearchService searchService;

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
}
