package com.campus.user.controller;

import com.campus.core.common.BusinessException;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import com.campus.user.dto.ChangePasswordRequest;
import com.campus.user.dto.LoginRequest;
import com.campus.user.dto.LoginResponse;
import com.campus.user.dto.UpdateProfileRequest;
import com.campus.user.entity.User;
import com.campus.user.service.UserService;
import com.campus.activity.util.FileUploadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Api(tags = "用户管理")
public class UserController {

    private final UserService userService;
    private final FileUploadUtil fileUploadUtil;

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<LoginResponse> login(@Validated({CreateGroup.class, UpdateGroup.class}) @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取用户信息")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<Void> register(@Validated({CreateGroup.class}) @RequestBody User user) {
        userService.register(user, user.getSecurityQuestionId(), user.getSecurityAnswer());
        return Result.success(null, "注册成功");
    }

    @PutMapping("/password")
    @ApiOperation("修改密码")
    public Result<Void> changePassword(
            HttpServletRequest request,
            @Validated({UpdateGroup.class}) @RequestBody ChangePasswordRequest changeRequest) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        userService.changePassword(userId, changeRequest.getOldPassword(), changeRequest.getNewPassword());
        return Result.success(null, "密码修改成功");
    }

    /**
     * 获取当前用户个人信息
     * 根据用户故事1.2.1 - 查看个人信息
     */
    @GetMapping("/profile")
    @ApiOperation("获取当前用户个人信息")
    public Result<User> getCurrentUserProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 修改个人资料
     * 根据用户故事1.2.2 - 修改个人资料
     */
    @PutMapping("/profile")
    @ApiOperation("修改个人资料")
    public Result<Void> updateProfile(
            HttpServletRequest request,
            @Validated({UpdateGroup.class}) @RequestBody UpdateProfileRequest updateRequest) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        userService.updateProfile(userId, updateRequest.getRealName(), updateRequest.getContact());
        return Result.success(null, "个人资料修改成功");
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    @ApiOperation("上传用户头像")
    public Result<Map<String, Object>> uploadAvatar(
            HttpServletRequest request,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }

        try {
            // 使用 FileUploadUtil 统一安全上传（含类型白名单、Magic Number校验、路径遍历防护）
            String avatarUrl = fileUploadUtil.uploadImageToSubDir(file, "avatars", String.valueOf(userId));
            userService.updateAvatar(userId, avatarUrl);

            Map<String, Object> data = new HashMap<>();
            data.put("avatarUrl", avatarUrl);
            return Result.success(data, "头像上传成功");
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR, "头像上传失败");
        }
    }

    /**
     * 获取用户头像
     */
    @GetMapping("/{id}/avatar")
    @ApiOperation("获取用户头像")
    public Result<Map<String, Object>> getUserAvatar(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("userId", id);
        data.put("avatarUrl", user.getAvatar());
        return Result.success(data);
    }
}
