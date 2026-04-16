package com.campus.user.controller;

import com.campus.core.common.Result;
import com.campus.user.dto.ResetPasswordRequest;
import com.campus.user.dto.SecurityQuestion;
import com.campus.user.dto.SetSecurityRequest;
import com.campus.user.dto.VerifySecurityRequest;
import com.campus.user.mapper.UserMapper;
import com.campus.user.service.UserSecurityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户密保Controller
 */
@RestController
@RequestMapping("/api/v1/users/security")
@RequiredArgsConstructor
@Validated
@Api(tags = "密保管理")
public class UserSecurityController {

    private final UserSecurityService userSecurityService;
    private final UserMapper userMapper;

    /**
     * 获取密保问题列表
     */
    @GetMapping("/questions")
    @ApiOperation("获取密保问题列表")
    public Result<List<SecurityQuestion>> getSecurityQuestions() {
        return Result.success(userSecurityService.getSecurityQuestions());
    }

    /**
     * 获取当前用户的密保问题
     */
    @GetMapping("/user/{userId}")
    @ApiOperation("获取当前用户的密保问题")
    public Result<SecurityQuestion> getUserSecurityQuestion(@PathVariable Long userId) {
        SecurityQuestion question = userSecurityService.getSecurityQuestionByUserId(userId);
        if (question == null) {
            return Result.error(404, "该用户未设置密保问题");
        }
        return Result.success(question);
    }

    /**
     * 根据用户名获取密保问题（找回密码用）
     */
    @GetMapping("/username/{username}")
    @ApiOperation("根据用户名获取密保问题")
    public Result<SecurityQuestion> getSecurityQuestionByUsername(@PathVariable String username) {
        try {
            SecurityQuestion question = userSecurityService.getSecurityQuestionByUsername(username);
            return Result.success(question);
        } catch (RuntimeException e) {
            return Result.error(404, e.getMessage());
        }
    }

    /**
     * 设置或修改密保
     */
    @PostMapping("/set")
    @ApiOperation("设置密保问题")
    public Result<Void> setSecurity(@Valid @RequestBody SetSecurityRequest request) {
        try {
            userSecurityService.setSecurity(
                request.getUserId(),
                request.getSecurityQuestionId(),
                request.getSecurityAnswer()
            );
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 验证密保答案（用于找回密码时验证）
     */
    @PostMapping("/verify")
    @ApiOperation("验证密保答案")
    public Result<Void> verifySecurityAnswer(@Valid @RequestBody VerifySecurityRequest request) {
        try {
            var user = userMapper.selectByUsername(request.getUsername());
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            boolean verified = userSecurityService.verifyAnswer(user.getId(), request.getSecurityAnswer());
            if (verified) {
                return Result.success();
            } else {
                return Result.error(401, "密保答案错误");
            }
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    @ApiOperation("重置密码")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            userSecurityService.resetPassword(
                request.getUsername(),
                request.getSecurityAnswer(),
                request.getNewPassword()
            );
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
