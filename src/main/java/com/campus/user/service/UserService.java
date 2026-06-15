package com.campus.user.service;

import com.campus.core.common.BusinessException;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.PasswordValidator;
import com.campus.core.common.ResultCode;
import com.campus.core.constants.AuditOperationConstants;
import com.campus.core.constants.AuditResourceTypeConstants;
import com.campus.core.constants.UserRoleConstants;
import com.campus.core.constants.UserStatusConstants;
import com.campus.core.service.AuditService;
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

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserMapper userMapper;
    private final UserSecurityService userSecurityService;
    private final LoginLockService loginLockService;
    private final JwtUtils jwtUtils;
    private final AuditService auditService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        long startTime = System.currentTimeMillis();

        // 检查用户是否被锁定
        if (loginLockService.isUserLocked(username)) {
            logger.warn("用户登录被锁定: {}", username);
            // 记录审计日志（登录失败）
            auditService.quickRecord(null, null, AuditOperationConstants.LOGIN,
                    AuditResourceTypeConstants.USER, null, 403, "登录失败次数过多，账号被锁定");
            throw new BusinessException(ResultCode.FORBIDDEN, "登录失败次数过多，请15分钟后再试");
        }

        User user = userMapper.selectByUsername(username);
        if (user == null) {
            // 记录审计日志（用户不存在）
            auditService.quickRecord(null, null, AuditOperationConstants.LOGIN,
                    AuditResourceTypeConstants.USER, null, 404, "用户不存在");
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 检查用户状态（是否被禁用）
        if (UserStatusConstants.isDisabled(user.getStatus())) {
            logger.warn("用户登录失败: {}, 用户已被禁用", username);
            // 记录审计日志（用户被禁用）
            auditService.quickRecord(user.getId(), null, AuditOperationConstants.LOGIN,
                    AuditResourceTypeConstants.USER, user.getId(), 403, "用户已被禁用，禁止登录");
            throw new BusinessException(ResultCode.AUTHENTICATION_ACCOUNT_DISABLED, "账户已被禁用，请联系管理员");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 记录登录失败
            int failCount = loginLockService.recordLoginFailure(username);
            logger.warn("用户登录失败: {}, 失败次数: {}", username, failCount);
            
            // 记录审计日志（密码错误）
            auditService.quickRecord(user.getId(), null, AuditOperationConstants.LOGIN,
                    AuditResourceTypeConstants.USER, user.getId(), 401, "密码错误，失败次数: " + failCount);
            
            // 检查是否达到锁定阈值
            if (failCount >= loginLockService.getMaxLoginFailCount()) {
                loginLockService.lockUser(username);
                throw new BusinessException(ResultCode.FORBIDDEN, "登录失败次数过多，已锁定15分钟");
            }
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 登录成功，清除失败记录
        loginLockService.clearLoginFailure(username);
        logger.info("用户登录成功: {}", username);

        // 记录审计日志（登录成功）
        int executionTime = (int) (System.currentTimeMillis() - startTime);
        auditService.quickRecord(user.getId(), null, AuditOperationConstants.LOGIN,
                AuditResourceTypeConstants.USER, user.getId(), 200, "登录成功");

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(user.getId(), user.getUsername(), user.getRealName(), user.getRole(), token);
    }

    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    public void register(User user, Integer securityQuestionId, String securityAnswer) {
        User existUser = userMapper.selectByUsername(user.getUsername());
        if (existUser != null) {
            // 记录审计日志（用户已存在）
            auditService.quickRecord(null, null, AuditOperationConstants.REGISTER,
                    AuditResourceTypeConstants.USER, null, 409, "用户名已存在");
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
        user.setRole(UserRoleConstants.USER);
        user.setStatus(UserStatusConstants.ENABLED); // 设置默认状态为启用
        userMapper.insert(user);

        userSecurityService.setSecurityOnRegister(user.getId(), securityQuestionId, securityAnswer);

        // 记录审计日志（注册成功）
        auditService.quickRecord(user.getId(), null, AuditOperationConstants.REGISTER,
                AuditResourceTypeConstants.USER, user.getId(), 200, "用户注册成功");
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
            // 记录审计日志（用户不存在）
            auditService.quickRecord(userId, null, AuditOperationConstants.PASSWORD_CHANGE,
                    AuditResourceTypeConstants.USER, userId, 404, "用户不存在");
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            // 记录审计日志（旧密码错误）
            auditService.quickRecord(userId, null, AuditOperationConstants.PASSWORD_CHANGE,
                    AuditResourceTypeConstants.USER, userId, 401, "旧密码错误");
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "旧密码错误");
        }
        PasswordValidator.validate(newPassword);
        user.setPassword(hashPassword(newPassword));
        userMapper.updateById(user);

        // 记录审计日志（密码修改成功）
        auditService.quickRecord(userId, null, AuditOperationConstants.PASSWORD_CHANGE,
                AuditResourceTypeConstants.USER, userId, 200, "密码修改成功");
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
