package com.campus.core.validation.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * 强密码验证器
 * 验证密码强度：至少8位，包含大小写字母、数字和特殊字符
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    // 至少8位，包含大小写字母、数字和特殊字符
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$"
    );

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // 使用 @NotBlank 来验证非空
        }
        return PASSWORD_PATTERN.matcher(value).matches();
    }
}
