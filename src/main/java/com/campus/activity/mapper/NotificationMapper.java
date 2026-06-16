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
}
