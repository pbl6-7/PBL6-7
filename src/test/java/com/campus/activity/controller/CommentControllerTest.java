package com.campus.activity.controller;

import com.campus.activity.dto.CommentRequest;
import com.campus.activity.dto.CommentResponse;
import com.campus.activity.entity.Comment;
import com.campus.activity.service.CommentService;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private CommentController commentController;

    private Comment testComment;
    private CommentRequest commentRequest;
    private CommentResponse commentResponse;
    private static final String VALID_TOKEN = "Bearer valid-token";

    @BeforeEach
    void setUp() {
        testComment = new Comment();
        testComment.setId(1L);
        testComment.setActivityId(1L);
        testComment.setUserId(1L);
        testComment.setContent("这是一条测试评论");
        testComment.setCreatedAt(LocalDateTime.now());

        commentRequest = new CommentRequest();
        commentRequest.setContent("新评论内容");

        commentResponse = new CommentResponse();
        commentResponse.setId(1L);
        commentResponse.setActivityId(1L);
        commentResponse.setUserId(1L);
        commentResponse.setUsername("testuser");
        commentResponse.setContent("这是一条测试评论");
        commentResponse.setCreatedAt(LocalDateTime.now());

        when(jwtUtils.validateToken("valid-token")).thenReturn(true);
        when(jwtUtils.getUserIdFromToken("valid-token")).thenReturn(1L);
    }

    @Test
    void testPublishComment_Success() {
        when(commentService.publishComment(anyLong(), anyLong(), any(CommentRequest.class)))
                .thenReturn(commentResponse);

        Result<CommentResponse> result = commentController.publishComment(VALID_TOKEN, 1L, commentRequest);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("这是一条测试评论", result.getData().getContent());
        verify(commentService, times(1)).publishComment(1L, 1L, commentRequest);
    }

    @Test
    void testPublishComment_Unauthorized() {
        Result<CommentResponse> result = commentController.publishComment(null, 1L, commentRequest);

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), result.getCode());
    }

    @Test
    void testGetCommentList_Success() {
        List<CommentResponse> comments = Arrays.asList(commentResponse);
        when(commentService.getCommentList(anyLong(), anyInt(), anyInt()))
                .thenReturn(comments);

        Result<List<CommentResponse>> result = commentController.getCommentList(1L, 1, 10);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
    }

    @Test
    void testGetCommentList_Empty() {
        when(commentService.getCommentList(anyLong(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList());

        Result<List<CommentResponse>> result = commentController.getCommentList(1L, 1, 10);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void testDeleteComment_Success() {
        doNothing().when(commentService).deleteComment(anyLong(), anyLong());

        Result<Void> result = commentController.deleteComment(VALID_TOKEN, 1L);

        assertEquals(200, result.getCode());
        verify(commentService, times(1)).deleteComment(1L, 1L);
    }

    @Test
    void testDeleteComment_Unauthorized() {
        Result<Void> result = commentController.deleteComment(null, 1L);

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), result.getCode());
    }
}
