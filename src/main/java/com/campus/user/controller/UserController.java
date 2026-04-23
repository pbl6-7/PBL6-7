package com.campus.user.controller;

import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.user.dto.ChangePasswordRequest;
import com.campus.user.dto.LoginRequest;
import com.campus.user.dto.LoginResponse;
import com.campus.user.entity.User;
import com.campus.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Api(tags = "用户管理")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
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
    public Result<Void> register(@RequestBody User user) {
        userService.register(user, user.getSecurityQuestionId(), user.getSecurityAnswer());
        return Result.success(null, "注册成功");
    }

    @PutMapping("/password")
    @ApiOperation("修改密码")
    public Result<Void> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ChangePasswordRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success(null, "密码修改成功");
    }
}
