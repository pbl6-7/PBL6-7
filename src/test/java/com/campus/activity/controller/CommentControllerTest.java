package com.campus.activity.controller;

import com.campus.activity.dto.CommentRequest;
import com.campus.activity.dto.CommentResponse;
import com.campus.activity.entity.Comment;
import com.campus.activity.service.CommentService;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private User testUser;
    private Comment testComment;
    private CommentRequest commentRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
        testUser.setRole("user");

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setActivityId(1L);
        testComment.setUserId(1L);
        testComment.setContent("这是一条测试评论");
        testComment.setCreateTime(LocalDateTime.now());

        commentRequest = new CommentRequest();
        commentRequest.setContent("新评论内容");
    }

    @Test
    void testAddComment_Success() {
        when(commentService.addComment(anyLong(), anyLong(), any(CommentRequest.class)))
                .thenReturn(testComment);

        ResponseEntity<Result<Comment>> response =
                commentController.addComment(1L, commentRequest, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("这是一条测试评论", response.getBody().getData().getContent());
        verify(commentService, times(1)).addComment(eq(1L), eq(1L), any());
    }

    @Test
    void testAddComment_ActivityNotFound() {
        when(commentService.addComment(anyLong(), anyLong(), any()))
                .thenThrow(new com.campus.core.common.BusinessException(ResultCode.NOT_FOUND, "活动不存在"));

        ResponseEntity<Result<Comment>> response =
                commentController.addComment(999L, commentRequest, testUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetActivityComments_Success() {
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setRecords(Arrays.asList(testComment));
        commentResponse.setTotal(1L);
        when(commentService.getActivityComments(anyLong(), anyInt(), anyInt()))
                .thenReturn(commentResponse);

        ResponseEntity<Result<CommentResponse>> response =
                commentController.getActivityComments(1L, 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().getRecords().size());
    }

    @Test
    void testGetActivityComments_Pagination() {
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setRecords(Arrays.asList(testComment));
        commentResponse.setTotal(50L);
        when(commentService.getActivityComments(anyLong(), anyInt(), anyInt()))
                .thenReturn(commentResponse);

        ResponseEntity<Result<CommentResponse>> response =
                commentController.getActivityComments(1L, 2, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(50L, response.getBody().getData().getTotal());
    }

    @Test
    void testDeleteComment_Success() {
        when(commentService.deleteComment(anyLong(), anyLong())).thenReturn(true);

        ResponseEntity<Result<Void>> response =
                commentController.deleteComment(1L, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(commentService, times(1)).deleteComment(1L, 1L);
    }

    @Test
    void testDeleteComment_NotOwner() {
        when(commentService.deleteComment(anyLong(), anyLong()))
                .thenThrow(new com.campus.core.common.BusinessException(ResultCode.FORBIDDEN, "无权限删除"));

        ResponseEntity<Result<Void>> response =
                commentController.deleteComment(1L, testUser);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testDeleteComment_CommentNotFound() {
        when(commentService.deleteComment(anyLong(), anyLong()))
                .thenThrow(new com.campus.core.common.BusinessException(ResultCode.NOT_FOUND, "评论不存在"));

        ResponseEntity<Result<Void>> response =
                commentController.deleteComment(999L, testUser);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteComment_AdminCanDelete() {
        testUser.setRole("admin");
        when(commentService.deleteComment(1L, 1L)).thenReturn(true);

        ResponseEntity<Result<Void>> response =
                commentController.deleteComment(1L, testUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetUserComments_Success() {
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setRecords(Arrays.asList(testComment));
        when(commentService.getUserComments(anyLong(), anyInt(), anyInt()))
                .thenReturn(commentResponse);

        ResponseEntity<Result<CommentResponse>> response =
                commentController.getUserComments(testUser, 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().getRecords().size());
    }
}
