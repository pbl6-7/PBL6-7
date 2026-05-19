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

    int updateIsRead(@Param("id") Long id);

    Notification selectById(@Param("id") Long id);

    List<Notification> selectByUserId(@Param("userId") Long userId, @Param("offset") Long offset, @Param("limit") Long limit);

    int countByUserId(@Param("userId") Long userId);

    int countUnreadByUserId(@Param("userId") Long userId);

    List<Notification> selectByActivityId(@Param("activityId") Long activityId);
}
