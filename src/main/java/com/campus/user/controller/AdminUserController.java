package com.campus.user.controller;

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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Api(tags = "管理员-用户管理")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @ApiOperation("获取用户列表（分页）")
    public Result<UserPageResponse> getUserPageList(
            HttpServletRequest request,
            @ModelAttribute UserPageRequest pageRequest) {
        Long adminId = (Long) request.getAttribute("currentUserId");
        UserPageResponse response = adminUserService.getUserPageList(pageRequest, adminId);
        return Result.success(response);
    }

    @GetMapping("/all")
    @ApiOperation("获取所有用户")
    public Result<List<UserResponse>> getAllUsers(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("currentUserId");
        List<UserResponse> users = adminUserService.getAllUsers(adminId);
        return Result.success(users);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取用户详情")
    public Result<UserResponse> getUserById(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long adminId = (Long) request.getAttribute("currentUserId");
        UserResponse user = adminUserService.getUserById(id, adminId);
        return Result.success(user);
    }

    @GetMapping("/role/{role}")
    @ApiOperation("按角色获取用户")
    public Result<List<UserResponse>> getUsersByRole(
            HttpServletRequest request,
            @PathVariable String role) {
        List<UserResponse> users = adminUserService.getUsersByRole(role);
        return Result.success(users);
    }

    @PutMapping("/{id}/role")
    @ApiOperation("更新用户角色")
    public Result<Void> updateUserRole(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest roleRequest) {
        Long currentUserId = (Long) request.getAttribute("currentUserId");
        if (currentUserId != null && currentUserId.equals(id)) {
            return Result.error(ResultCode.BAD_REQUEST, "不能修改自己的角色");
        }

        adminUserService.updateUserRole(id, roleRequest.getRole());
        return Result.success(null, "用户角色更新成功");
    }
}
