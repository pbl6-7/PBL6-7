package com.campus.activity.controller;

import com.campus.activity.dto.SearchSuggestionResponse;
import com.campus.activity.service.SearchService;
import com.campus.core.common.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController searchController;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetSearchSuggestions_Success() {
        SearchSuggestionResponse suggestion = new SearchSuggestionResponse();
        suggestion.setSuggestions(Arrays.asList("校园宣讲会", "校园招聘会", "校园晚会"));
        suggestion.setHotSearches(Arrays.asList("招聘会", "宣讲会"));
        when(searchService.getSearchSuggestions("校园")).thenReturn(suggestion);

        Result<SearchSuggestionResponse> result = searchController.getSearchSuggestions("校园");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(3, result.getData().getSuggestions().size());
    }

    @Test
    void testGetSearchSuggestions_EmptyPrefix() {
        SearchSuggestionResponse suggestion = new SearchSuggestionResponse();
        suggestion.setSuggestions(Arrays.asList());
        suggestion.setHotSearches(Arrays.asList("招聘会", "宣讲会"));
        when(searchService.getSearchSuggestions("")).thenReturn(suggestion);

        Result<SearchSuggestionResponse> result = searchController.getSearchSuggestions("");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().getSuggestions().isEmpty());
    }

    @Test
    void testAutocomplete_Success() {
        List<String> suggestions = Arrays.asList("学术讲座", "学术会议", "学术交流");
        when(searchService.autocomplete("学术")).thenReturn(suggestions);

        Result<List<String>> result = searchController.autocomplete("学术");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(3, result.getData().size());
    }

    @Test
    void testAutocomplete_NoMatch() {
        when(searchService.autocomplete("xyz")).thenReturn(Arrays.asList());

        Result<List<String>> result = searchController.autocomplete("xyz");

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void testGetHotSearches_Success() {
        List<String> hotTerms = Arrays.asList("招聘会", "宣讲会", "体育赛事");
        when(searchService.getHotSearches()).thenReturn(hotTerms);

        Result<List<String>> result = searchController.getHotSearches();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(3, result.getData().size());
        assertEquals("招聘会", result.getData().get(0));
    }

    @Test
    void testGetHotSearches_Empty() {
        when(searchService.getHotSearches()).thenReturn(Arrays.asList());

        Result<List<String>> result = searchController.getHotSearches();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }
}
