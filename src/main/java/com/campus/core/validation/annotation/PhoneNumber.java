package com.campus.core.validation.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 手机号验证注解
 * 验证手机号格式，应为11位数字且以1开头
 */
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumber {
    String message() default "手机号格式不正确，应为11位数字且以1开头";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
