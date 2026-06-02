package com.campus.activity.mapper;

import com.campus.activity.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CommentMapper {
    /**
     * 插入评论
     * @param comment 评论
     * @return 插入的记录数
     */
    int insert(Comment comment);

    /**
     * 根据ID查询评论
     * @param id 评论ID
     * @return 评论
     */
    Comment selectById(@Param("id") Long id);

    /**
     * 查询活动的评论列表（分页）
     * @param activityId 活动ID
     * @param offset 偏移量
     * @param size 每页数量
     * @return 评论列表
     */
    List<Comment> selectByActivityId(
            @Param("activityId") Long activityId,
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );

    /**
     * 查询活动的评论总数
     * @param activityId 活动ID
     * @return 评论总数
     */
    Long countByActivityId(@Param("activityId") Long activityId);

    /**
     * 删除评论
     * @param id 评论ID
     * @return 删除的记录数
     */
    int deleteById(@Param("id") Long id);
}
