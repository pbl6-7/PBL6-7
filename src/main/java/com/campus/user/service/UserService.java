package com.campus.user.service;

import com.campus.core.common.BusinessException;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.PasswordValidator;
import com.campus.core.common.ResultCode;
import com.campus.user.dto.LoginRequest;
import com.campus.user.dto.LoginResponse;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import com.campus.user.service.UserSecurityService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserMapper userMapper;
    private final UserSecurityService userSecurityService;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final ConcurrentHashMap<String, Integer> loginFailCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> loginLockTime = new ConcurrentHashMap<>();
    private static final int MAX_LOGIN_FAIL_COUNT = 5;
    private static final long LOGIN_LOCK_DURATION = 15 * 60 * 1000;

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();

        if (isLoginLocked(username)) {
            logger.warn("用户登录被锁定: {}", username);
            throw new BusinessException(ResultCode.FORBIDDEN, "登录失败次数过多，请15分钟后再试");
        }

        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginFailCount.merge(username, 1, Integer::sum);
            int failCount = loginFailCount.getOrDefault(username, 0);
            logger.warn("用户登录失败: {}, 失败次数: {}", username, failCount);
            if (failCount >= MAX_LOGIN_FAIL_COUNT) {
                loginLockTime.put(username, System.currentTimeMillis());
                loginFailCount.remove(username);
                throw new BusinessException(ResultCode.FORBIDDEN, "登录失败次数过多，已锁定15分钟");
            }
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        loginFailCount.remove(username);
        loginLockTime.remove(username);
        logger.info("用户登录成功: {}", username);

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(user.getId(), user.getUsername(), user.getRealName(), user.getRole(), token);
    }

    private boolean isLoginLocked(String username) {
        Long lockTime = loginLockTime.get(username);
        if (lockTime == null) {
            return false;
        }
        if (System.currentTimeMillis() - lockTime > LOGIN_LOCK_DURATION) {
            loginLockTime.remove(username);
            return false;
        }
        return true;
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
        validatePasswordStrength(user.getPassword());
        user.setPassword(hashPassword(user.getPassword()));
        user.setRole("user");
        userMapper.insert(user);

        userSecurityService.setSecurity(user.getId(), securityQuestionId, securityAnswer);
    }

    private String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 验证密码强度
     * 委托给统一的 PasswordValidator 工具类
     */
    private void validatePasswordStrength(String password) {
        PasswordValidator.validate(password);
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
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "旧密码错误");
        }
        PasswordValidator.validate(newPassword);
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
