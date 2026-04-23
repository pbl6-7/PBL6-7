package com.campus.user.mapper;

import com.campus.user.entity.UserSecurity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserSecurityMapper {
    /**
     * 根据用户ID查询密保信息
     */
    UserSecurity selectByUserId(@Param("userId") Long userId);

    /**
     * 插入密保信息
     */
    int insert(UserSecurity userSecurity);

    /**
     * 更新密保信息
     */
    int updateByUserId(UserSecurity userSecurity);

    /**
     * 根据用户ID删除密保信息
     */
    int deleteByUserId(@Param("userId") Long userId);
}
