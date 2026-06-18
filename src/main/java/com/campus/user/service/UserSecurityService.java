package com.campus.user.service;

import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import com.campus.core.common.PasswordValidator;
import com.campus.user.dto.SecurityQuestion;
import com.campus.user.entity.UserSecurity;
import com.campus.user.mapper.UserSecurityMapper;
import com.campus.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * 用户密保Service
 */
@Service
@RequiredArgsConstructor
public class UserSecurityService {

    private final UserSecurityMapper userSecurityMapper;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 密保验证失败计数（自动过期，防止内存泄漏） */
    private final Cache<Long, Integer> verifyFailCount = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();
    /** 密保验证锁定时间（自动过期，防止内存泄漏） */
    private final Cache<Long, Long> verifyLockTime = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();
    private static final int MAX_VERIFY_FAIL_COUNT = 3;
    private static final long VERIFY_LOCK_DURATION = 5 * 60 * 1000;

    private static final List<SecurityQuestion> SECURITY_QUESTIONS = Arrays.asList(
        new SecurityQuestion(1, "您就读的小学名称是什么？"),
        new SecurityQuestion(2, "您的父亲叫什么名字？"),
        new SecurityQuestion(3, "您的母亲叫什么名字？"),
        new SecurityQuestion(4, "您的生日是什么？（格式：yyyy-MM-dd）"),
        new SecurityQuestion(5, "您最好的朋友叫什么名字？"),
        new SecurityQuestion(6, "您的家乡在哪里？"),
        new SecurityQuestion(7, "您的第一份工作是什么？"),
        new SecurityQuestion(8, "您最喜欢的动物是什么？")
    );

    /**
     * 获取所有密保问题列表
     */
    public List<SecurityQuestion> getSecurityQuestions() {
        return SECURITY_QUESTIONS;
    }

    /**
     * 根据用户ID获取密保问题
     */
    public SecurityQuestion getSecurityQuestionByUserId(Long userId) {
        UserSecurity userSecurity = userSecurityMapper.selectByUserId(userId);
        if (userSecurity == null) {
            return null;
        }
        return SECURITY_QUESTIONS.stream()
                .filter(q -> q.getQuestionId().equals(userSecurity.getSecurityQuestionId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 设置或修改密保（需验证当前密码）
     * @param userId 用户ID
     * @param password 当前登录密码
     * @param securityQuestionId 密保问题ID
     * @param securityAnswer 密保答案
     */
    public void setSecurity(Long userId, String password, Integer securityQuestionId, String securityAnswer) {
        if (userId == null || securityQuestionId == null || securityAnswer == null || password == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "参数不能为空");
        }

        // 验证当前密码
        var user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR, "密码错误");
        }

        doSetSecurity(userId, securityQuestionId, securityAnswer);
    }

    /**
     * 注册时设置密保（无需验证密码，仅限注册流程内部调用）
     * @param userId 用户ID
     * @param securityQuestionId 密保问题ID
     * @param securityAnswer 密保答案
     */
    public void setSecurityOnRegister(Long userId, Integer securityQuestionId, String securityAnswer) {
        if (userId == null || securityQuestionId == null || securityAnswer == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "参数不能为空");
        }
        doSetSecurity(userId, securityQuestionId, securityAnswer);
    }

    /**
     * 实际执行密保设置
     */
    private void doSetSecurity(Long userId, Integer securityQuestionId, String securityAnswer) {
        if (securityQuestionId < 1 || securityQuestionId > 8) {
            throw new BusinessException(ResultCode.SECURITY_QUESTION_INVALID);
        }

        UserSecurity existingSecurity = userSecurityMapper.selectByUserId(userId);
        String hashedAnswer = hashAnswer(securityAnswer);

        if (existingSecurity != null) {
            existingSecurity.setSecurityQuestionId(securityQuestionId);
            existingSecurity.setSecurityAnswer(hashedAnswer);
            userSecurityMapper.updateByUserId(existingSecurity);
        } else {
            UserSecurity userSecurity = new UserSecurity();
            userSecurity.setUserId(userId);
            userSecurity.setSecurityQuestionId(securityQuestionId);
            userSecurity.setSecurityAnswer(hashedAnswer);
            userSecurityMapper.insert(userSecurity);
        }
    }

    /**
     * 验证密保答案
     */
    public boolean verifyAnswer(Long userId, String answer) {
        if (isVerifyLocked(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "密保验证次数过多，请5分钟后再试");
        }
        UserSecurity userSecurity = userSecurityMapper.selectByUserId(userId);
        if (userSecurity == null) {
            throw new BusinessException(ResultCode.SECURITY_QUESTION_NOT_SET);
        }
        boolean matches = passwordEncoder.matches(answer, userSecurity.getSecurityAnswer());
        if (!matches) {
            verifyFailCount.put(userId, verifyFailCount.asMap().getOrDefault(userId, 0) + 1);
            int failCount = verifyFailCount.asMap().getOrDefault(userId, 0);
            if (failCount >= MAX_VERIFY_FAIL_COUNT) {
                verifyLockTime.put(userId, System.currentTimeMillis());
                verifyFailCount.invalidate(userId);
                throw new BusinessException(ResultCode.FORBIDDEN, "密保验证失败次数过多，已锁定5分钟");
            }
        } else {
            verifyFailCount.invalidate(userId);
            verifyLockTime.invalidate(userId);
        }
        return matches;
    }

    private boolean isVerifyLocked(Long userId) {
        Long lockTime = verifyLockTime.getIfPresent(userId);
        if (lockTime == null) {
            return false;
        }
        if (System.currentTimeMillis() - lockTime > VERIFY_LOCK_DURATION) {
            // 锁定过期时同时清理失败计数和锁定时间
            verifyLockTime.invalidate(userId);
            verifyFailCount.invalidate(userId);
            return false;
        }
        return true;
    }

    /**
     * 根据用户名获取密保问题（用于找回密码）
     */
    public SecurityQuestion getSecurityQuestionByUsername(String username) {
        var user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return getSecurityQuestionByUserId(user.getId());
    }

    /**
     * 根据用户名验证密保答案并重置密码
     */
    @Transactional
    public void resetPassword(String username, String securityAnswer, String newPassword) {
        var user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!verifyAnswer(user.getId(), securityAnswer)) {
            throw new BusinessException(ResultCode.SECURITY_ANSWER_ERROR);
        }

        // 验证新密码强度（与注册时一致）
        PasswordValidator.validate(newPassword);

        String hashedPassword = hashPassword(newPassword);
        user.setPassword(hashedPassword);
        userMapper.updateById(user);
    }

    /**
     * 密码加密
     * @param password 原始密码
     * @return 加密后的密码
     */
    private String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 密保答案加密
     */
    private String hashAnswer(String answer) {
        return passwordEncoder.encode(answer);
    }
}
