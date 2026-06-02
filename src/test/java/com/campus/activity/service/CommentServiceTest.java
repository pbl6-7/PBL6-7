package com.campus.activity.service;

import com.campus.activity.dto.CommentRequest;
import com.campus.activity.dto.CommentResponse;
import com.campus.activity.entity.Comment;
import com.campus.activity.mapper.CommentMapper;
import com.campus.core.common.BusinessException;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 评论功能测试
 */
public class CommentServiceTest {

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private Comment testComment;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setActivityId(1L);
        testComment.setUserId(1L);
        testComment.setContent("测试评论");
        testComment.setReplyToId(null);
    }

    /**
     * 测试正常发布评论
     */
    @Test
    public void testPublishCommentSuccess() {
        CommentRequest request = new CommentRequest();
        request.setContent("新评论");

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(commentMapper.insert(any(Comment.class))).thenReturn(1);

        CommentResponse response = commentService.publishComment(1L, 1L, request);

        assertNotNull(response);
        assertEquals("新评论", response.getContent());
        assertEquals("测试用户", response.getUsername());
        verify(commentMapper).insert(any(Comment.class));
    }

    /**
     * 测试发布评论-用户不存在
     */
    @Test
    public void testPublishCommentUserNotFound() {
        CommentRequest request = new CommentRequest();
        request.setContent("新评论");

        when(userMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.publishComment(1L, 999L, request);
        });

        assertEquals(4001, exception.getCode());
    }

    /**
     * 测试发布评论-回复不存在
     */
    @Test
    public void testPublishCommentReplyNotFound() {
        CommentRequest request = new CommentRequest();
        request.setContent("回复评论");
        request.setReplyToId(999L);

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(commentMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.publishComment(1L, 1L, request);
        });

        assertEquals(404, exception.getCode());
    }

    /**
     * 测试发布评论-回复不属于该活动
     */
    @Test
    public void testPublishCommentReplyNotBelongToActivity() {
        CommentRequest request = new CommentRequest();
        request.setContent("回复评论");
        request.setReplyToId(1L);

        Comment parentComment = new Comment();
        parentComment.setId(1L);
        parentComment.setActivityId(2L);

        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(commentMapper.selectById(1L)).thenReturn(parentComment);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.publishComment(1L, 1L, request);
        });

        assertTrue(exception.getMessage().contains("不属于该活动"));
    }

    /**
     * 测试获取评论列表
     */
    @Test
    public void testGetCommentList() {
        List<Comment> comments = Arrays.asList(testComment);
        when(commentMapper.selectRootCommentsByActivityId(1L, 0, 10)).thenReturn(comments);
        when(commentMapper.selectRepliesByReplyToId(1L)).thenReturn(Collections.emptyList());
        when(userMapper.selectBatchIds(anyList())).thenReturn(Arrays.asList(testUser));

        List<CommentResponse> responses = commentService.getCommentList(1L, 1, 10);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    /**
     * 测试获取评论列表-空列表
     */
    @Test
    public void testGetCommentListEmpty() {
        when(commentMapper.selectRootCommentsByActivityId(1L, 0, 10)).thenReturn(Collections.emptyList());

        List<CommentResponse> responses = commentService.getCommentList(1L, 1, 10);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    /**
     * 测试删除评论-成功
     */
    @Test
    public void testDeleteCommentSuccess() {
        when(commentMapper.selectById(1L)).thenReturn(testComment);
        when(commentMapper.selectAllRepliesByReplyToId(1L)).thenReturn(Collections.emptyList());
        when(commentMapper.deleteBatchByIds(anyList())).thenReturn(1);

        assertDoesNotThrow(() -> {
            commentService.deleteComment(1L, 1L);
        });

        verify(commentMapper).deleteBatchByIds(anyList());
    }

    /**
     * 测试删除评论-评论不存在
     */
    @Test
    public void testDeleteCommentNotFound() {
        when(commentMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.deleteComment(999L, 1L);
        });

        assertEquals(404, exception.getCode());
    }

    /**
     * 测试删除评论-无权限
     */
    @Test
    public void testDeleteCommentNoPermission() {
        when(commentMapper.selectById(1L)).thenReturn(testComment);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.deleteComment(1L, 999L);
        });

        assertEquals(403, exception.getCode());
    }

    /**
     * 测试删除评论-递归删除子回复
     */
    @Test
    public void testDeleteCommentWithReplies() {
        Comment replyComment = new Comment();
        replyComment.setId(2L);
        replyComment.setActivityId(1L);
        replyComment.setUserId(2L);
        replyComment.setContent("回复");
        replyComment.setReplyToId(1L);

        when(commentMapper.selectById(1L)).thenReturn(testComment);
        when(commentMapper.selectAllRepliesByReplyToId(1L)).thenReturn(Arrays.asList(replyComment));
        when(commentMapper.selectAllRepliesByReplyToId(2L)).thenReturn(Collections.emptyList());
        when(commentMapper.deleteBatchByIds(anyList())).thenReturn(1);

        assertDoesNotThrow(() -> {
            commentService.deleteComment(1L, 1L);
        });

        verify(commentMapper).deleteBatchByIds(anyList());
    }
}
