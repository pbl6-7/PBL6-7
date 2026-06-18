package com.campus.user.mapper;

import com.campus.user.entity.LoginLock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 登录锁定Mapper接口
 * 提供登录锁定记录的数据库操作
 */
@Mapper
public interface LoginLockMapper {

    /**
     * 插入锁定记录
     */
    void insert(LoginLock loginLock);

    /**
     * 根据用户名查询有效锁定记录
     */
    LoginLock selectActiveByUsername(@Param("username") String username);

    /**
     * 解锁用户（更新锁定状态）
     */
    void unlockByUsername(@Param("username") String username);

    /**
     * 获取所有有效锁定记录
     */
    List<LoginLock> selectAllActive();

    /**
     * 删除过期锁定记录
     */
    void deleteExpired();

    /**
     * 更新失败次数
     */
    void updateFailCount(@Param("username") String username, @Param("failCount") int failCount);

    /**
     * 获取用户失败次数
     */
    Integer selectFailCount(@Param("username") String username);

    /**
     * 插入或更新失败次数
     */
    void upsertFailCount(@Param("username") String username, @Param("failCount") int failCount);
}
