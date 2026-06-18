package com.campus.core.validation.annotation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 文件上传验证注解
 * 验证上传文件的大小和类型
 */
@Documented
@Constraint(validatedBy = FileValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface File {
    long maxSize() default 5 * 1024 * 1024; // 默认5MB
    String[] allowedTypes() default {};
    String message() default "文件上传失败";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
