package com.campus.core.validation.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 无敏感词验证注解
 * 验证文本内容不包含敏感词
 */
@Documented
@Constraint(validatedBy = NoSensitiveWordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoSensitiveWord {
    String message() default "内容包含敏感词";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
