package com.campus.activity.mapper;

import com.campus.activity.entity.ActivityRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ActivityRegistrationMapper {

    int insert(ActivityRegistration registration);

    int updateById(ActivityRegistration registration);

    int deleteById(@Param("id") Long id);

    int deleteByActivityIdAndUserId(@Param("activityId") Long activityId, @Param("userId") Long userId);

    ActivityRegistration selectById(@Param("id") Long id);

    ActivityRegistration selectByActivityIdAndUserId(@Param("activityId") Long activityId, @Param("userId") Long userId);

    List<ActivityRegistration> selectByUserId(@Param("userId") Long userId);

    List<ActivityRegistration> selectByActivityId(@Param("activityId") Long activityId);

    List<ActivityRegistration> selectByActivityIdAndStatus(@Param("activityId") Long activityId, @Param("status") String status);

    Long countByActivityId(@Param("activityId") Long activityId);

    Long countByActivityIdAndStatus(@Param("activityId") Long activityId, @Param("status") String status);

    List<ActivityRegistration> selectByUserIdWithPage(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("size") Integer size);

    Long countByUserId(@Param("userId") Long userId);
}
