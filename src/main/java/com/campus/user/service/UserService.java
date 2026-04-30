package com.campus.user.service;

import com.campus.core.common.BusinessException;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.ResultCode;
import com.campus.user.dto.LoginRequest;
import com.campus.user.dto.LoginResponse;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import com.campus.user.service.UserSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserSecurityService userSecurityService;
    private final JwtUtils jwtUtils;

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        String hashedPassword = hashPassword(request.getPassword());
        if (!hashedPassword.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(user.getId(), user.getUsername(), user.getRealName(), user.getRole(), token);
    }

    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    public void register(User user, Integer securityQuestionId, String securityAnswer) {
        User existUser = userMapper.selectByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }
        if (securityQuestionId == null || securityAnswer == null || securityAnswer.trim().isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "请设置密保问题");
        }
        if (securityQuestionId < 1 || securityQuestionId > 8) {
            throw new BusinessException(ResultCode.SECURITY_QUESTION_INVALID);
        }
        user.setPassword(hashPassword(user.getPassword()));
        user.setRole("user");
        userMapper.insert(user);

        userSecurityService.setSecurity(user.getId(), securityQuestionId, securityAnswer);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "密码加密失败");
        }
    }

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        String hashedOldPassword = hashPassword(oldPassword);
        if (!hashedOldPassword.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "旧密码错误");
        }
        user.setPassword(hashPassword(newPassword));
        userMapper.updateById(user);
    }

    /**
     * 更新个人资料
     * @param userId 用户ID
     * @param realName 真实姓名
     * @param contact 联系方式
     */
    public void updateProfile(Long userId, String realName, String contact) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (realName != null && !realName.trim().isEmpty()) {
            user.setRealName(realName.trim());
        }
        if (contact != null) {
            user.setContact(contact.trim());
        }
        userMapper.updateById(user);
    }
}
