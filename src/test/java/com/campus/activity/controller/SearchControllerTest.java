package com.campus.activity.controller;

import com.campus.activity.dto.SearchSuggestionResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.service.SearchService;
import com.campus.core.common.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    private Activity testActivity;

    @BeforeEach
    void setUp() {
        testActivity = new Activity();
        testActivity.setId(1L);
        testActivity.setTitle("校园招聘会");
        testActivity.setDescription("2026年春季校园招聘会");
        testActivity.setLocation("体育馆");
        testActivity.setStatus("published");
    }

    @Test
    void testGetSearchSuggestions_Success() {
        SearchSuggestionResponse suggestion = new SearchSuggestionResponse();
        suggestion.setSuggestions(Arrays.asList("校园宣讲会", "校园招聘会", "校园晚会"));
        when(searchService.getSearchSuggestions(anyString())).thenReturn(suggestion);

        ResponseEntity<Result<SearchSuggestionResponse>> response =
                searchController.getSearchSuggestions("校园");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getData().getSuggestions().size());
    }

    @Test
    void testGetSearchSuggestions_EmptyPrefix() {
        SearchSuggestionResponse suggestion = new SearchSuggestionResponse();
        suggestion.setSuggestions(Arrays.asList());
        when(searchService.getSearchSuggestions(anyString())).thenReturn(suggestion);

        ResponseEntity<Result<SearchSuggestionResponse>> response =
                searchController.getSearchSuggestions("");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getData().getSuggestions().isEmpty());
    }

    @Test
    void testAutocomplete_Success() {
        List<String> suggestions = Arrays.asList("学术讲座", "学术会议", "学术交流");
        when(searchService.autocomplete(anyString())).thenReturn(suggestions);

        ResponseEntity<Result<List<String>>> response =
                searchController.autocomplete("学术");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getData().size());
    }

    @Test
    void testAutocomplete_NoMatch() {
        when(searchService.autocomplete(anyString())).thenReturn(Arrays.asList());

        ResponseEntity<Result<List<String>>> response =
                searchController.autocomplete("xyz");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getData().isEmpty());
    }

    @Test
    void testGetHotSearchTerms_Success() {
        List<String> hotTerms = Arrays.asList("招聘会", "宣讲会", "体育赛事");
        when(searchService.getHotSearchTerms()).thenReturn(hotTerms);

        ResponseEntity<Result<List<String>>> response = searchController.getHotSearchTerms();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getData().size());
        assertEquals("招聘会", response.getBody().getData().get(0));
    }

    @Test
    void testGetHotSearchTerms_Empty() {
        when(searchService.getHotSearchTerms()).thenReturn(Arrays.asList());

        ResponseEntity<Result<List<String>>> response = searchController.getHotSearchTerms();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getData().isEmpty());
    }

    @Test
    void testIncrementSearchCount_Success() {
        doNothing().when(searchService).incrementSearchCount(anyString());

        ResponseEntity<Result<Void>> response =
                searchController.incrementSearchCount("校园");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(searchService, times(1)).incrementSearchCount("校园");
    }
}
