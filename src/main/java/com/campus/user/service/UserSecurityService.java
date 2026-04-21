package com.campus.user.service;

import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import com.campus.user.dto.SecurityQuestion;
import com.campus.user.entity.UserSecurity;
import com.campus.user.mapper.UserSecurityMapper;
import com.campus.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * 用户密保Service
 */
@Service
@RequiredArgsConstructor
public class UserSecurityService {

    private final UserSecurityMapper userSecurityMapper;
    private final UserMapper userMapper;

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
     * 设置密保问题
     */
    public void setSecurity(Long userId, Integer securityQuestionId, String securityAnswer) {
        if (userId == null || securityQuestionId == null || securityAnswer == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "参数不能为空");
        }
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
        UserSecurity userSecurity = userSecurityMapper.selectByUserId(userId);
        if (userSecurity == null) {
            throw new BusinessException(ResultCode.SECURITY_QUESTION_NOT_SET);
        }
        String hashedAnswer = hashAnswer(answer);
        return hashedAnswer.equals(userSecurity.getSecurityAnswer());
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
    public void resetPassword(String username, String securityAnswer, String newPassword) {
        var user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!verifyAnswer(user.getId(), securityAnswer)) {
            throw new BusinessException(ResultCode.SECURITY_ANSWER_ERROR);
        }

        String hashedPassword = hashPassword(newPassword);
        user.setPassword(hashedPassword);
        userMapper.updateById(user);
    }

    /**
     * 密码加密
     */
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
     * 密保答案加密
     */
    private String hashAnswer(String answer) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(answer.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "加密失败");
        }
    }
}
