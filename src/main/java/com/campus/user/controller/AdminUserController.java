package com.campus.user.controller;

import com.campus.core.common.BusinessException;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.core.constants.UserRoleConstants;
import com.campus.user.dto.UpdateRoleRequest;
import com.campus.user.dto.UserPageRequest;
import com.campus.user.dto.UserPageResponse;
import com.campus.user.dto.UserResponse;
import com.campus.user.service.AdminUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员-用户管理控制器
 * 所有接口需要管理员权限
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Api(tags = "管理员-用户管理")
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 验证管理员权限
     */
    private void validateAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        String role = (String) request.getAttribute("currentUserRole");
        if (userId == null || role == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        if (!UserRoleConstants.ADMIN.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "需要管理员权限");
        }
    }

    @GetMapping
    @ApiOperation("获取用户列表（分页）")
    public Result<UserPageResponse> getUserPageList(
            HttpServletRequest request,
            @ModelAttribute UserPageRequest pageRequest) {
        validateAdmin(request);
        Long adminId = (Long) request.getAttribute("currentUserId");
        UserPageResponse response = adminUserService.getUserPageList(pageRequest, adminId);
        return Result.success(response);
    }

    @GetMapping("/all")
    @ApiOperation("获取所有用户")
    public Result<List<UserResponse>> getAllUsers(HttpServletRequest request) {
        validateAdmin(request);
        Long adminId = (Long) request.getAttribute("currentUserId");
        List<UserResponse> users = adminUserService.getAllUsers(adminId);
        return Result.success(users);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取用户详情")
    public Result<UserResponse> getUserById(
            HttpServletRequest request,
            @PathVariable Long id) {
        validateAdmin(request);
        Long adminId = (Long) request.getAttribute("currentUserId");
        UserResponse user = adminUserService.getUserById(id, adminId);
        return Result.success(user);
    }

    @GetMapping("/role/{role}")
    @ApiOperation("按角色获取用户")
    public Result<List<UserResponse>> getUsersByRole(
            HttpServletRequest request,
            @PathVariable String role) {
        validateAdmin(request);
        List<UserResponse> users = adminUserService.getUsersByRole(role);
        return Result.success(users);
    }

    @PutMapping("/{id}/role")
    @ApiOperation("更新用户角色")
    public Result<Void> updateUserRole(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest roleRequest) {
        validateAdmin(request);
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        if (currentUserId != null && currentUserId.equals(id)) {
            return Result.error(ResultCode.BAD_REQUEST, "不能修改自己的角色");
        }

        adminUserService.updateUserRole(id, roleRequest.getRole());
        return Result.success(null, "用户角色更新成功");
    }

    /**
     * 更新用户状态（启用/禁用）
     */
    @PutMapping("/{id}/status")
    @ApiOperation("更新用户状态（启用/禁用）")
    public Result<Void> updateUserStatus(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        validateAdmin(request);
        Long adminId = (Long) request.getAttribute("currentUserId");
        String status = body.get("status");
        if ("enabled".equals(status)) {
            adminUserService.enableUser(id, adminId);
        } else if ("disabled".equals(status)) {
            adminUserService.disableUser(id, adminId);
        } else {
            return Result.error(ResultCode.BAD_REQUEST, "无效的状态值，应为enabled或disabled");
        }
        return Result.success(null, "用户状态更新成功");
    }

    /**
     * 批量操作用户
     */
    @PostMapping("/batch")
    @ApiOperation("批量操作用户")
    public Result<com.campus.user.dto.BatchOperationResponse> batchOperation(
            HttpServletRequest request,
            @RequestBody Map<String, Object> body) {
        validateAdmin(request);
        Long adminId = (Long) request.getAttribute("currentUserId");
        String operation = (String) body.get("operation");
        @SuppressWarnings("unchecked")
        List<Long> userIds = (List<Long>) body.get("userIds");

        if (operation == null || userIds == null || userIds.isEmpty()) {
            return Result.error(ResultCode.BAD_REQUEST, "操作类型和用户ID列表不能为空");
        }

        com.campus.user.dto.BatchOperationResponse response;
        switch (operation) {
            case "enable":
                response = adminUserService.batchEnableUsers(userIds, adminId);
                break;
            case "disable":
                response = adminUserService.batchDisableUsers(userIds, adminId);
                break;
            case "delete":
                response = adminUserService.batchDeleteUsers(userIds, adminId);
                break;
            default:
                return Result.error(ResultCode.BAD_REQUEST, "不支持的操作类型: " + operation);
        }
        return Result.success(response, "批量操作完成");
    }

    /**
     * 获取锁定用户列表
     * 返回被禁用（锁定）的用户列表
     */
    @GetMapping("/locked")
    @ApiOperation("获取锁定用户列表")
    public Result<UserPageResponse> getLockedUsers(
            HttpServletRequest request,
            @ModelAttribute UserPageRequest pageRequest) {
        validateAdmin(request);
        Long adminId = (Long) request.getAttribute("currentUserId");
        UserPageResponse users = adminUserService.getDisabledUsers(pageRequest, adminId);
        return Result.success(users);
    }

    /**
     * 解锁用户
     */
    @PutMapping("/{id}/unlock")
    @ApiOperation("解锁用户")
    public Result<Void> unlockUser(
            HttpServletRequest request,
            @PathVariable Long id) {
        validateAdmin(request);
        Long adminId = (Long) request.getAttribute("currentUserId");
        adminUserService.enableUser(id, adminId);
        return Result.success(null, "用户已解锁");
    }

    /**
     * 获取权限列表
     */
    @GetMapping("/permissions")
    @ApiOperation("获取权限列表")
    public Result<List<Map<String, Object>>> getPermissions(HttpServletRequest request) {
        validateAdmin(request);
        List<Map<String, Object>> permissions = new ArrayList<>();

        // 用户权限
        Map<String, Object> userPerm = new HashMap<>();
        userPerm.put("role", "USER");
        userPerm.put("permissions", Arrays.asList(
                "activity:publish", "activity:edit:own", "activity:delete:own",
                "registration:create", "registration:cancel:own",
                "comment:publish", "comment:delete:own",
                "collect:add", "collect:remove",
                "subscription:add", "subscription:remove",
                "notification:read", "notification:delete:own",
                "search:execute", "search:history",
                "profile:view", "profile:edit", "profile:changePassword",
                "avatar:upload"
        ));
        permissions.add(userPerm);

        // 管理员权限
        Map<String, Object> adminPerm = new HashMap<>();
        adminPerm.put("role", "ADMIN");
        adminPerm.put("permissions", Arrays.asList(
                "activity:publish", "activity:edit:all", "activity:delete:all",
                "activity:approve", "activity:reject",
                "registration:create", "registration:cancel:all", "registration:update:status",
                "comment:publish", "comment:delete:all",
                "collect:add", "collect:remove",
                "subscription:add", "subscription:remove",
                "notification:read", "notification:delete:all", "notification:broadcast",
                "search:execute", "search:history",
                "profile:view", "profile:edit", "profile:changePassword",
                "avatar:upload",
                "user:view:all", "user:status:update", "user:role:update", "user:batch:operate",
                "sensitive-word:manage", "tag:manage", "type:manage",
                "statistics:view", "monitor:view", "cache:manage",
                "audit:view", "login-lock:manage"
        ));
        permissions.add(adminPerm);

        return Result.success(permissions);
    }
}
