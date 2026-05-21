package com.campus.user.controller;

import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.user.dto.UpdateRoleRequest;
import com.campus.user.dto.UserPageRequest;
import com.campus.user.dto.UserPageResponse;
import com.campus.user.dto.UserResponse;
import com.campus.user.service.AdminUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Api(tags = "管理员-用户管理")
public class AdminUserController {

    private static final String ROLE_ADMIN = "admin";

    private final AdminUserService adminUserService;
    private final JwtUtils jwtUtils;

    /**
     * 验证管理员权限
     */
    private void validateAdminRole(String token) {
        if (!jwtUtils.validateToken(token)) {
            throw new com.campus.core.common.BusinessException(ResultCode.TOKEN_INVALID);
        }
        String role = jwtUtils.getRoleFromToken(token);
        if (!ROLE_ADMIN.equals(role)) {
            throw new com.campus.core.common.BusinessException(ResultCode.NOT_ADMIN);
        }
    }

    @GetMapping
    @ApiOperation("获取用户列表（分页）")
    public Result<UserPageResponse> getUserPageList(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @ModelAttribute UserPageRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        UserPageResponse response = adminUserService.getUserPageList(request);
        return Result.success(response);
    }

    @GetMapping("/all")
    @ApiOperation("获取所有用户")
    public Result<List<UserResponse>> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        List<UserResponse> users = adminUserService.getAllUsers();
        return Result.success(users);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取用户详情")
    public Result<UserResponse> getUserById(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        UserResponse user = adminUserService.getUserById(id);
        return Result.success(user);
    }

    @GetMapping("/role/{role}")
    @ApiOperation("按角色获取用户")
    public Result<List<UserResponse>> getUsersByRole(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String role) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        List<UserResponse> users = adminUserService.getUsersByRole(role);
        return Result.success(users);
    }

    @PutMapping("/{id}/role")
    @ApiOperation("更新用户角色")
    public Result<Void> updateUserRole(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        validateAdminRole(token);

        Long currentUserId = jwtUtils.getUserIdFromToken(token);
        if (currentUserId != null && currentUserId.equals(id)) {
            return Result.error(ResultCode.BAD_REQUEST, "不能修改自己的角色");
        }

        adminUserService.updateUserRole(id, request.getRole());
        return Result.success(null, "用户角色更新成功");
    }
}
