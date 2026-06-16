package com.campus.user.controller;

import com.campus.core.common.BusinessException;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.core.constants.UserRoleConstants;
import com.campus.user.service.LoginLockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 管理员-登录锁定管理控制器
 * 提供登录锁定记录查询和解除锁定功能
 */
@RestController
@RequestMapping("/api/admin/login-lock")
@RequiredArgsConstructor
@Api(tags = "管理员-登录锁定管理")
public class AdminLoginLockController {

    private final LoginLockService loginLockService;

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

    /**
     * 获取登录锁定记录列表
     */
    @GetMapping("/list")
    @ApiOperation("获取登录锁定记录列表")
    public Result<List<Map<String, Object>>> getLockedList(HttpServletRequest request) {
        validateAdmin(request);
        List<Map<String, Object>> lockedList = loginLockService.getLockedList();
        return Result.success(lockedList);
    }

    /**
     * 解除用户登录锁定
     */
    @DeleteMapping("/{username}")
    @ApiOperation("解除用户登录锁定")
    public Result<Void> unlockUser(
            HttpServletRequest request,
            @PathVariable String username) {
        validateAdmin(request);
        loginLockService.unlockUser(username);
        return Result.success(null, "用户登录锁定已解除");
    }
}
