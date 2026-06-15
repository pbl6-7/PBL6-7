package com.campus.activity.service;

import com.campus.activity.dto.SearchSuggestionResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.mapper.ActivityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 搜索功能测试
 */
public class SearchServiceTest {

    @Mock
    private ActivityMapper activityMapper;

    @InjectMocks
    private SearchService searchService;

    private Activity testActivity;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testActivity = new Activity();
        testActivity.setId(1L);
        testActivity.setTitle("学术讲座：人工智能发展趋势");
        testActivity.setDescription("关于AI的最新技术发展");
        testActivity.setLocation("图书馆报告厅");
    }

    /**
     * 测试获取搜索建议和热门搜索-无前缀
     */
    @Test
    public void testGetSearchSuggestionsNoPrefix() {
        SearchSuggestionResponse response = searchService.getSearchSuggestions(null);

        assertNotNull(response);
        assertTrue(response.getSuggestions().isEmpty());
        assertNotNull(response.getHotSearches());
        assertEquals(10, response.getHotSearches().size());
    }

    /**
     * 测试获取搜索建议和热门搜索-有前缀
     */
    @Test
    public void testGetSearchSuggestionsWithPrefix() {
        List<Activity> activities = Arrays.asList(testActivity);
        when(activityMapper.selectList(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(activities);

        SearchSuggestionResponse response = searchService.getSearchSuggestions("学术");

        assertNotNull(response);
        assertNotNull(response.getHotSearches());
        assertEquals(10, response.getHotSearches().size());
    }

    /**
     * 测试获取搜索建议和热门搜索-空前缀
     */
    @Test
    public void testGetSearchSuggestionsEmptyPrefix() {
        SearchSuggestionResponse response = searchService.getSearchSuggestions("");

        assertNotNull(response);
        assertTrue(response.getSuggestions().isEmpty());
        assertNotNull(response.getHotSearches());
    }

    /**
     * 测试自动补全-无前缀
     */
    @Test
    public void testAutocompleteNoPrefix() {
        List<String> result = searchService.autocomplete(null);

        assertNotNull(result);
        assertEquals(10, result.size());
    }

    /**
     * 测试自动补全-有前缀匹配
     */
    @Test
    public void testAutocompleteWithPrefix() {
        List<Activity> activities = Arrays.asList(testActivity);
        when(activityMapper.selectList(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(Integer.class), any(Integer.class)))
                .thenReturn(activities);

        List<String> result = searchService.autocomplete("学术");

        assertNotNull(result);
    }

    /**
     * 测试自动补全-空前缀
     */
    @Test
    public void testAutocompleteEmptyPrefix() {
        List<String> result = searchService.autocomplete("");

        assertNotNull(result);
        assertEquals(10, result.size());
    }
}
