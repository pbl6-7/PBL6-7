package com.campus.core.validation.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 强密码验证注解
 * 验证密码强度，至少需要8位，包含大小写字母、数字和特殊字符
 */
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "密码强度不足，至少需要8位，包含大小写字母、数字和特殊字符";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
