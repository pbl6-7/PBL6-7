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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
     * 密保验证Token存储
     * key: username, value: {token, expireTime}
     */
    private final Map<String, TokenRecord> verifyTokenStore = new ConcurrentHashMap<>();

    /**
     * Token有效期（毫秒），10分钟
     */
    private static final long TOKEN_EXPIRE_MS = 10 * 60 * 1000;

    /**
     * Token记录内部类
     */
    private static class TokenRecord {
        String token;
        long expireTime;

        TokenRecord(String token, long expireTime) {
            this.token = token;
            this.expireTime = expireTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

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
     * 设置或修改密保（需验证当前密码）
     * userId从JWT获取，防止伪造
     */
    @PostMapping("/set")
    @ApiOperation("设置密保问题")
    public Result<Void> setSecurity(HttpServletRequest httpRequest,
                                     @Valid @RequestBody SetSecurityRequest request) {
        Long userId = (Long) httpRequest.getAttribute("currentUserId");
        userSecurityService.setSecurity(
            userId,
            request.getPassword(),
            request.getSecurityQuestionId(),
            request.getSecurityAnswer()
        );
        return Result.success(null, "密保设置成功");
    }

    /**
     * 验证密保答案（用于找回密码时验证）
     * 验证成功后返回一次性Token，用于后续重置密码
     */
    @PostMapping("/verify")
    @ApiOperation("验证密保答案")
    public Result<Map<String, Object>> verifySecurityAnswer(@Valid @RequestBody VerifySecurityRequest request) {
        var user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        boolean verified = userSecurityService.verifyAnswer(user.getId(), request.getSecurityAnswer());
        if (verified) {
            // 生成一次性验证Token
            String verifyToken = UUID.randomUUID().toString().replace("-", "");
            verifyTokenStore.put(request.getUsername(), new TokenRecord(verifyToken, System.currentTimeMillis() + TOKEN_EXPIRE_MS));

            Map<String, Object> data = new java.util.HashMap<>();
            data.put("verifyToken", verifyToken);
            data.put("message", "验证成功，请使用verifyToken重置密码");
            return Result.success(data, "验证成功");
        } else {
            return Result.error(ResultCode.SECURITY_ANSWER_ERROR);
        }
    }

    /**
     * 重置密码
     * 需要验证密保验证Token，确保密保验证和密码重置是关联的
     */
    @PostMapping("/reset-password")
    @ApiOperation("重置密码")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // 验证密保验证Token
        TokenRecord tokenRecord = verifyTokenStore.get(request.getUsername());
        if (tokenRecord == null || tokenRecord.isExpired() || !tokenRecord.token.equals(request.getVerifyToken())) {
            return Result.error(ResultCode.BAD_REQUEST, "密保验证Token无效或已过期，请重新验证密保");
        }

        // Token使用后立即删除（一次性Token）
        verifyTokenStore.remove(request.getUsername());

        userSecurityService.resetPassword(
            request.getUsername(),
            request.getSecurityAnswer(),
            request.getNewPassword()
        );
        return Result.success(null, "密码重置成功");
    }
}
