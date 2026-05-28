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
     * 获取用户分页列表
     * @param request 分页请求参数
     * @return 用户分页响应
     */
    public UserPageResponse getUserPageList(UserPageRequest request) {
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
     * 获取所有用户列表
     * @return 用户列表
     */
    public List<UserResponse> getAllUsers() {
        return userMapper.selectAllUsers().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取用户详情
     * @param id 用户ID
     * @return 用户信息
     */
    public UserResponse getUserById(Long id) {
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
}
