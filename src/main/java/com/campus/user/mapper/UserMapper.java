package com.campus.user.mapper;

import com.campus.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
     * 批量查询用户
     * @param ids 用户ID列表
     * @return 用户列表
     */
    List<User> selectBatchIds(@Param("ids") List<Long> ids);
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
    /**
     * 查询所有用户（管理员功能）
     * @return 用户列表
     */
    List<User> selectAllUsers();
    /**
     * 按角色查询用户
     * @param role 用户角色
     * @return 用户列表
     */
    List<User> selectUsersByRole(@Param("role") String role);
    /**
     * 分页查询用户
     * @param keyword 关键词
     * @param role 角色
     * @param offset 偏移量
     * @param size 每页数量
     * @return 用户列表
     */
    List<User> selectUserPageList(
        @Param("keyword") String keyword,
        @Param("role") String role,
        @Param("offset") Integer offset,
        @Param("size") Integer size
    );
    /**
     * 统计用户总数
     * @param keyword 关键词
     * @param role 角色
     * @return 用户总数
     */
    Long countUsers(
        @Param("keyword") String keyword,
        @Param("role") String role
    );
    /**
     * 更新用户角色
     * @param id 用户ID
     * @param role 新角色
     * @return 更新的记录数
     */
    int updateUserRole(@Param("id") Long id, @Param("role") String role);

    /**
     * 统计所有用户数量
     * @return 用户总数
     */
    Long countAllUsers();

    /**
     * 按角色统计用户数量
     * @param role 用户角色
     * @return 用户数量
     */
    Long countUsersByRole(@Param("role") String role);
}
