package com.campus.user.service;

import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import com.campus.user.dto.UserPageRequest;
import com.campus.user.dto.UserPageResponse;
import com.campus.user.dto.UserResponse;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final List<String> VALID_ROLES = Arrays.asList("user", "publisher", "admin");

    private final UserMapper userMapper;

    /**
     * 获取用户分页列表（需要管理员权限）
     * @param request 分页请求参数
     * @param adminId 管理员ID（用于权限验证）
     * @return 用户分页响应
     */
    public UserPageResponse getUserPageList(UserPageRequest request, Long adminId) {
        validateAdminPermission(adminId);
        
        Integer page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
        Integer size = request.getSize() != null && request.getSize() > 0 ? request.getSize() : 10;
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }

        Integer offset = (page - 1) * size;
        List<User> users = userMapper.selectUserPageList(request.getKeyword(), request.getRole(), offset, size);
        Long total = userMapper.countUsers(request.getKeyword(), request.getRole());

        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());

        UserPageResponse response = new UserPageResponse();
        response.setList(userResponses);
        response.setTotal(total);
        response.setPage(page);
        response.setSize(size);
        response.setTotalPages((int) Math.ceil((double) total / size));

        return response;
    }

    /**
     * 获取所有用户列表（需要管理员权限）
     * @param adminId 管理员ID（用于权限验证）
     * @return 用户列表
     */
    public List<UserResponse> getAllUsers(Long adminId) {
        validateAdminPermission(adminId);
        return userMapper.selectAllUsers().stream()
                .limit(MAX_PAGE_SIZE)
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取用户详情（需要管理员权限）
     * @param id 用户ID
     * @param adminId 管理员ID（用于权限验证）
     * @return 用户信息
     */
    public UserResponse getUserById(Long id, Long adminId) {
        validateAdminPermission(adminId);
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return UserResponse.fromEntity(user);
    }

    /**
     * 按角色获取用户列表
     * @param role 用户角色
     * @return 用户列表
     */
    public List<UserResponse> getUsersByRole(String role) {
        if (!VALID_ROLES.contains(role)) {
            throw new BusinessException(ResultCode.INVALID_ROLE);
        }
        return userMapper.selectUsersByRole(role).stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 更新用户角色
     * @param userId 用户ID
     * @param newRole 新角色
     */
    @Transactional
    public void updateUserRole(Long userId, String newRole) {
        if (!VALID_ROLES.contains(newRole)) {
            throw new BusinessException(ResultCode.INVALID_ROLE);
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        userMapper.updateUserRole(userId, newRole);
    }

    /**
     * 验证管理员权限
     * @param adminId 管理员ID
     */
    private void validateAdminPermission(Long adminId) {
        if (adminId == null) {
            throw new BusinessException(ResultCode.FORBIDDEN, "管理员ID不能为空");
        }
        User admin = userMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "管理员不存在");
        }
        if (!"admin".equals(admin.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无管理员权限");
        }
    }
}
