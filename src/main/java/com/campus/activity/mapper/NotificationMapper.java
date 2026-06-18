package com.campus.activity.mapper;

import com.campus.activity.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 通知Mapper接口
 * 提供通知相关的数据访问方法
 */
@Mapper
public interface NotificationMapper {
    int insert(Notification notification);

    int batchInsert(@Param("list") List<Notification> notifications);

    int updateIsRead(@Param("id") Long id);

    Notification selectById(@Param("id") Long id);

    List<Notification> selectByUserId(@Param("userId") Long userId, @Param("offset") Long offset, @Param("limit") Long limit);

    int countByUserId(@Param("userId") Long userId);

    int countUnreadByUserId(@Param("userId") Long userId);

    List<Notification> selectByActivityId(@Param("activityId") Long activityId);

    /**
     * 游标分页查询用户通知（基于lastId）
     * @param userId 用户ID
     * @param lastId 上一次查询的最后一条记录ID（用于游标分页）
     * @param size 每页数量
     * @return 通知列表
     */
    List<Notification> selectByUserIdWithCursor(@Param("userId") Long userId, @Param("lastId") Long lastId, @Param("size") Integer size);

    /**
     * 标记用户所有通知为已读
     * @param userId 用户ID
     * @return 更新记录数
     */
    int updateAllReadByUserId(@Param("userId") Long userId);

    /**
     * 根据ID删除通知
     * @param id 通知ID
     */
    int deleteById(@Param("id") Long id);

    /**
     * 删除过期的已读通知
     */
    void deleteOldNotifications(@Param("cutoff") java.time.LocalDateTime cutoff);

    /**
     * 查询所有通知（管理员用，分页）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 通知列表
     */
    List<Notification> selectAllRecent(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计通知总数
     * @return 通知总数
     */
    Long countAll();

    /**
     * 查询去重后的系统公告列表（按title+content分组，只取每个公告的一条记录）
     * @param type 通知类型（如 SYSTEM_ANNOUNCEMENT）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 去重后的公告通知列表
     */
    List<Notification> selectDistinctByType(@Param("type") String type, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计去重后的系统公告数量
     * @param type 通知类型
     * @return 去重后的公告数量
     */
    Long countDistinctByType(@Param("type") String type);

    /**
     * 根据标题和内容删除所有匹配的通知（用于删除公告时清理所有用户的通知副本）
     * @param title 通知标题
     * @param content 通知内容
     * @return 删除的记录数
     */
    int deleteByTitleAndContent(@Param("title") String title, @Param("content") String content);
}
