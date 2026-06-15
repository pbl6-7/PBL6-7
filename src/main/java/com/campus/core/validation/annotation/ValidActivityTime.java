package com.campus.core.validation.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 有效活动时间验证注解
 * 验证活动开始时间必须早于结束时间
 */
@Documented
@Constraint(validatedBy = ValidActivityTimeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidActivityTime {
    String message() default "活动开始时间必须早于结束时间";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
