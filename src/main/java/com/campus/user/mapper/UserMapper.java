package com.campus.user.mapper;

import com.campus.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户
     */ 
    User selectByUsername(@Param("username") String username);
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户
     */ 
    User selectById(@Param("id") Long id);
    /**
     * 插入用户
     * @param user 用户
     * @return 插入的用户ID
     */ 
    int insert(User user);
    /**
     * 更新用户
     * @param user 用户
     * @return 更新的用户ID
     */ 
    int updateById(User user);
    /**
     * 删除用户
     * @param id 用户ID
     * @return 删除的用户ID
     */ 
    int deleteById(@Param("id") Long id);
}
