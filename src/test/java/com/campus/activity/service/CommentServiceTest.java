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

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
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
        Comment testComment = new Comment();
        testComment.setId(1L);
        testComment.setActivityId(1L);
        testComment.setUserId(1L);
        testComment.setContent("测试评论");

        when(commentMapper.selectById(1L)).thenReturn(testComment);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.deleteComment(1L, 999L);
        });

        assertEquals(403, exception.getCode());
    }
}
