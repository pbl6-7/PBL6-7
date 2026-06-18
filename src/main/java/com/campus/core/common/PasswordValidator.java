package com.campus.core.common;

import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;

/**
 * 密码验证工具类
 * 提供密码强度验证功能
 */
public class PasswordValidator {

    /**
     * 最小密码长度
     */
    private static final int MIN_LENGTH = 6;

    /**
     * 最大密码长度
     */
    private static final int MAX_LENGTH = 50;

    /**
     * 验证密码强度
     *
     * @param password 密码
     * @throws BusinessException 当密码不符合要求时抛出异常
     */
    public static void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "密码不能为空");
        }

        if (password.length() < MIN_LENGTH) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR,
                    "密码长度不能少于" + MIN_LENGTH + "位");
        }

        if (password.length() > MAX_LENGTH) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR,
                    "密码长度不能超过" + MAX_LENGTH + "位");
        }
    }

    /**
     * 验证密码是否匹配
     *
     * @param rawPassword 原始密码
     * @param encodedPassword 编码后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        // 简单的比较，实际项目中应该使用 BCrypt 或其他加密方式
        return rawPassword.equals(encodedPassword) ||
               SHA256Util.encrypt(rawPassword).equals(encodedPassword);
    }
}
