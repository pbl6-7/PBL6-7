package com.campus.user.service;

import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import com.campus.core.constants.AuditOperationConstants;
import com.campus.core.constants.AuditResourceTypeConstants;
import com.campus.core.constants.UserRoleConstants;
import com.campus.core.constants.UserStatusConstants;
import com.campus.core.service.AuditService;
import com.campus.core.util.PageUtils;
import com.campus.user.dto.BatchOperationResponse;
import com.campus.user.dto.UserExportResponse;
import com.campus.user.dto.UserPageRequest;
import com.campus.user.dto.UserPageResponse;
import com.campus.user.dto.UserResponse;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 管理员用户服务类
 * 使用 PageUtils 优化分页处理
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserService.class);

    /**
     * 最大分页大小
     */
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * 有效角色列表
     */
    private static final List<String> VALID_ROLES = Arrays.asList(
            UserRoleConstants.USER,
            UserRoleConstants.PUBLISHER,
            UserRoleConstants.ADMIN
    );

    private final UserMapper userMapper;
    private final AuditService auditService;

    /**
     * 获取用户分页列表（需要管理员权限）
     *
     * @param request 分页请求参数
     * @param adminId 管理员ID（用于权限验证）
     * @return 用户分页响应
     * @throws BusinessException 当无管理员权限时抛出异常
     */
    public UserPageResponse getUserPageList(UserPageRequest request, Long adminId) {
        validateAdminPermission(adminId);

        // 使用 PageUtils 验证和规范化分页参数
        PageUtils.PageParams params = PageUtils.validateAndNormalize(
                request.getPage(), request.getSize());

        List<User> users = userMapper.selectUserPageList(
                request.getKeyword(), request.getRole(),
                params.getOffset(), params.getSize());
        Long total = userMapper.countUsers(request.getKeyword(), request.getRole());

        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());

        return buildUserPageResponse(userResponses, total, params);
    }

    /**
     * 获取所有用户列表（需要管理员权限）
     *
     * @param adminId 管理员ID（用于权限验证）
     * @return 用户列表
     * @throws BusinessException 当无管理员权限时抛出异常
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
     *
     * @param id 用户ID
     * @param adminId 管理员ID（用于权限验证）
     * @return 用户信息
     * @throws BusinessException 当用户不存在或无管理员权限时抛出异常
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
     *
     * @param role 用户角色
     * @return 用户列表
     * @throws BusinessException 当角色无效时抛出异常
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
     *
     * @param userId 用户ID
     * @param newRole 新角色
     * @throws BusinessException 当用户不存在或角色无效时抛出异常
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
     *
     * @param adminId 管理员ID
     * @throws BusinessException 当无管理员权限时抛出异常
     */
    private void validateAdminPermission(Long adminId) {
        if (adminId == null) {
            throw new BusinessException(ResultCode.FORBIDDEN, "管理员ID不能为空");
        }
        User admin = userMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND, "管理员不存在");
        }
        if (!UserRoleConstants.ADMIN.equals(admin.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无管理员权限");
        }
    }

    /**
     * 构建用户分页响应对象
     *
     * @param userResponses 用户响应列表
     * @param total 总记录数
     * @param params 分页参数
     * @return 用户分页响应对象
     */
    private UserPageResponse buildUserPageResponse(
            List<UserResponse> userResponses, Long total, PageUtils.PageParams params) {
        UserPageResponse response = new UserPageResponse();
        response.setList(userResponses);
        response.setTotal(total);
        response.setPage(params.getPage());
        response.setSize(params.getSize());
        response.setTotalPages(PageUtils.calculateTotalPages(total, params.getSize()));
        return response;
    }

    /**
     * 启用用户
     *
     * @param userId 用户ID
     * @param adminId 管理员ID
     * @throws BusinessException 当用户不存在或无权限时抛出异常
     */
    @Transactional
    public void enableUser(Long userId, Long adminId) {
        validateAdminPermission(adminId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (UserStatusConstants.isEnabled(user.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户已处于启用状态");
        }

        userMapper.updateUserStatus(userId, UserStatusConstants.ENABLED);
        
        // 记录审计日志
        auditService.quickRecord(adminId, user.getUsername(), AuditOperationConstants.USER_ENABLE,
                AuditResourceTypeConstants.USER, userId, 200, "启用用户");
        
        logger.info("管理员 {} 启用用户 {}", adminId, userId);
    }

    /**
     * 禁用用户
     *
     * @param userId 用户ID
     * @param adminId 管理员ID
     * @throws BusinessException 当用户不存在或无权限时抛出异常
     */
    @Transactional
    public void disableUser(Long userId, Long adminId) {
        validateAdminPermission(adminId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (UserStatusConstants.isDisabled(user.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "用户已处于禁用状态");
        }

        userMapper.updateUserStatus(userId, UserStatusConstants.DISABLED);
        
        // 记录审计日志
        auditService.quickRecord(adminId, user.getUsername(), AuditOperationConstants.USER_DISABLE,
                AuditResourceTypeConstants.USER, userId, 200, "禁用用户");
        
        logger.info("管理员 {} 禁用用户 {}", adminId, userId);
    }

    /**
     * 批量启用用户
     *
     * @param userIds 用户ID列表
     * @param adminId 管理员ID
     * @return 批量操作响应
     */
    @Transactional
    public BatchOperationResponse batchEnableUsers(List<Long> userIds, Long adminId) {
        validateAdminPermission(adminId);

        List<Long> failedUserIds = new ArrayList<>();
        List<String> failureReasons = new ArrayList<>();
        int successCount = 0;

        for (Long userId : userIds) {
            try {
                User user = userMapper.selectById(userId);
                if (user == null) {
                    failedUserIds.add(userId);
                    failureReasons.add("用户不存在");
                    continue;
                }

                if (UserStatusConstants.isEnabled(user.getStatus())) {
                    failedUserIds.add(userId);
                    failureReasons.add("用户已处于启用状态");
                    continue;
                }

                userMapper.updateUserStatus(userId, UserStatusConstants.ENABLED);
                successCount++;
                
                // 记录审计日志
                auditService.quickRecord(adminId, user.getUsername(), AuditOperationConstants.USER_ENABLE,
                        AuditResourceTypeConstants.USER, userId, 200, "批量启用用户");
            } catch (Exception e) {
                failedUserIds.add(userId);
                failureReasons.add("操作失败: " + e.getMessage());
                logger.error("批量启用用户失败: userId={}, error={}", userId, e.getMessage());
            }
        }

        logger.info("管理员 {} 批量启用用户: 成功 {}, 失败 {}", adminId, successCount, failedUserIds.size());
        return new BatchOperationResponse(successCount, failedUserIds.size(), failedUserIds, failureReasons);
    }

    /**
     * 批量禁用用户
     *
     * @param userIds 用户ID列表
     * @param adminId 管理员ID
     * @return 批量操作响应
     */
    @Transactional
    public BatchOperationResponse batchDisableUsers(List<Long> userIds, Long adminId) {
        validateAdminPermission(adminId);

        List<Long> failedUserIds = new ArrayList<>();
        List<String> failureReasons = new ArrayList<>();
        int successCount = 0;

        for (Long userId : userIds) {
            try {
                User user = userMapper.selectById(userId);
                if (user == null) {
                    failedUserIds.add(userId);
                    failureReasons.add("用户不存在");
                    continue;
                }

                if (UserStatusConstants.isDisabled(user.getStatus())) {
                    failedUserIds.add(userId);
                    failureReasons.add("用户已处于禁用状态");
                    continue;
                }

                userMapper.updateUserStatus(userId, UserStatusConstants.DISABLED);
                successCount++;
                
                // 记录审计日志
                auditService.quickRecord(adminId, user.getUsername(), AuditOperationConstants.USER_DISABLE,
                        AuditResourceTypeConstants.USER, userId, 200, "批量禁用用户");
            } catch (Exception e) {
                failedUserIds.add(userId);
                failureReasons.add("操作失败: " + e.getMessage());
                logger.error("批量禁用用户失败: userId={}, error={}", userId, e.getMessage());
            }
        }

        logger.info("管理员 {} 批量禁用用户: 成功 {}, 失败 {}", adminId, successCount, failedUserIds.size());
        return new BatchOperationResponse(successCount, failedUserIds.size(), failedUserIds, failureReasons);
    }

    /**
     * 获取禁用用户列表
     *
     * @param request 分页请求参数
     * @param adminId 管理员ID
     * @return 用户分页响应
     */
    public UserPageResponse getDisabledUsers(UserPageRequest request, Long adminId) {
        validateAdminPermission(adminId);

        PageUtils.PageParams params = PageUtils.validateAndNormalize(
                request.getPage(), request.getSize());

        List<User> users = userMapper.selectDisabledUsers(
                request.getKeyword(), params.getOffset(), params.getSize());
        Long total = userMapper.countDisabledUsers(request.getKeyword());

        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());

        return buildUserPageResponse(userResponses, total, params);
    }

    /**
     * 批量删除用户
     *
     * @param userIds 用户ID列表
     * @param adminId 管理员ID
     * @return 批量操作响应
     */
    @Transactional
    public BatchOperationResponse batchDeleteUsers(List<Long> userIds, Long adminId) {
        validateAdminPermission(adminId);

        List<Long> failedUserIds = new ArrayList<>();
        List<String> failureReasons = new ArrayList<>();
        int successCount = 0;

        for (Long userId : userIds) {
            try {
                User user = userMapper.selectById(userId);
                if (user == null) {
                    failedUserIds.add(userId);
                    failureReasons.add("用户不存在");
                    continue;
                }

                // 不能删除管理员用户（可选的安全限制）
                if (UserRoleConstants.ADMIN.equals(user.getRole())) {
                    failedUserIds.add(userId);
                    failureReasons.add("不能删除管理员用户");
                    continue;
                }

                userMapper.deleteById(userId);
                successCount++;
                
                // 记录审计日志
                auditService.quickRecord(adminId, user.getUsername(), AuditOperationConstants.USER_DELETE,
                        AuditResourceTypeConstants.USER, userId, 200, "批量删除用户");
            } catch (Exception e) {
                failedUserIds.add(userId);
                failureReasons.add("操作失败: " + e.getMessage());
                logger.error("批量删除用户失败: userId={}, error={}", userId, e.getMessage());
            }
        }

        logger.info("管理员 {} 批量删除用户: 成功 {}, 失败 {}", adminId, successCount, failedUserIds.size());
        return new BatchOperationResponse(successCount, failedUserIds.size(), failedUserIds, failureReasons);
    }

    /**
     * 批量更新用户角色
     *
     * @param userIds 用户ID列表
     * @param newRole 新角色
     * @param adminId 管理员ID
     * @return 批量操作响应
     */
    @Transactional
    public BatchOperationResponse batchUpdateUserRole(List<Long> userIds, String newRole, Long adminId) {
        validateAdminPermission(adminId);

        // 验证角色是否有效
        if (!VALID_ROLES.contains(newRole)) {
            throw new BusinessException(ResultCode.INVALID_ROLE);
        }

        List<Long> failedUserIds = new ArrayList<>();
        List<String> failureReasons = new ArrayList<>();
        int successCount = 0;

        for (Long userId : userIds) {
            try {
                User user = userMapper.selectById(userId);
                if (user == null) {
                    failedUserIds.add(userId);
                    failureReasons.add("用户不存在");
                    continue;
                }

                // 不能修改管理员角色（可选的安全限制）
                if (UserRoleConstants.ADMIN.equals(user.getRole())) {
                    failedUserIds.add(userId);
                    failureReasons.add("不能修改管理员角色");
                    continue;
                }

                userMapper.updateUserRole(userId, newRole);
                successCount++;
                
                // 记录审计日志
                auditService.quickRecord(adminId, user.getUsername(), AuditOperationConstants.ROLE_CHANGE,
                        AuditResourceTypeConstants.USER, userId, 200, "批量更新角色为: " + newRole);
            } catch (Exception e) {
                failedUserIds.add(userId);
                failureReasons.add("操作失败: " + e.getMessage());
                logger.error("批量更新用户角色失败: userId={}, error={}", userId, e.getMessage());
            }
        }

        logger.info("管理员 {} 批量更新用户角色: 成功 {}, 失败 {}", adminId, successCount, failedUserIds.size());
        return new BatchOperationResponse(successCount, failedUserIds.size(), failedUserIds, failureReasons);
    }

    /**
     * 导出用户数据
     *
     * @param adminId 管理员ID
     * @return 用户导出响应
     */
    public UserExportResponse exportUsers(Long adminId) {
        validateAdminPermission(adminId);

        List<User> users = userMapper.selectAllUsers();
        
        List<UserExportResponse.UserExportData> exportDataList = users.stream()
                .map(user -> {
                    UserExportResponse.UserExportData data = new UserExportResponse.UserExportData();
                    data.setId(user.getId());
                    data.setUsername(user.getUsername());
                    data.setRealName(user.getRealName());
                    data.setRole(user.getRole());
                    data.setStatus(user.getStatus());
                    data.setContact(user.getContact());
                    data.setCreatedAt(user.getCreatedAt());
                    data.setUpdatedAt(user.getUpdatedAt());
                    return data;
                })
                .collect(Collectors.toList());

        UserExportResponse response = new UserExportResponse();
        response.setExportTime(LocalDateTime.now());
        response.setAdminId(adminId);
        response.setTotalCount(users.size());
        response.setUsers(exportDataList);
        response.setFormat("JSON");
        
        // 记录审计日志
        auditService.quickRecord(adminId, null, AuditOperationConstants.DATA_EXPORT,
                AuditResourceTypeConstants.USER, null, 200, "导出用户数据，总数: " + users.size());
        
        logger.info("管理员 {} 导出用户数据: 总数 {}", adminId, users.size());
        return response;
    }
}