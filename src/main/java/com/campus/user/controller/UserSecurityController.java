package com.campus.user.controller;

import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
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
            return Result.error(ResultCode.SECURITY_QUESTION_NOT_SET);
        }
        return Result.success(question);
    }

    /**
     * 根据用户名获取密保问题（找回密码用）
     */
    @GetMapping("/username/{username}")
    @ApiOperation("根据用户名获取密保问题")
    public Result<SecurityQuestion> getSecurityQuestionByUsername(@PathVariable String username) {
        SecurityQuestion question = userSecurityService.getSecurityQuestionByUsername(username);
        return Result.success(question);
    }

    /**
     * 设置或修改密保
     */
    @PostMapping("/set")
    @ApiOperation("设置密保问题")
    public Result<Void> setSecurity(@Valid @RequestBody SetSecurityRequest request) {
        userSecurityService.setSecurity(
            request.getUserId(),
            request.getSecurityQuestionId(),
            request.getSecurityAnswer()
        );
        return Result.success(null, "密保设置成功");
    }

    /**
     * 验证密保答案（用于找回密码时验证）
     */
    @PostMapping("/verify")
    @ApiOperation("验证密保答案")
    public Result<Void> verifySecurityAnswer(@Valid @RequestBody VerifySecurityRequest request) {
        var user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        boolean verified = userSecurityService.verifyAnswer(user.getId(), request.getSecurityAnswer());
        if (verified) {
            return Result.success(null, "验证成功");
        } else {
            return Result.error(ResultCode.SECURITY_ANSWER_ERROR);
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    @ApiOperation("重置密码")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userSecurityService.resetPassword(
            request.getUsername(),
            request.getSecurityAnswer(),
            request.getNewPassword()
        );
        return Result.success(null, "密码重置成功");
    }
}
