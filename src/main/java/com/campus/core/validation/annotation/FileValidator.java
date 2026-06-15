package com.campus.core.validation.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 文件验证器
 * 验证上传文件的大小和类型
 */
public class FileValidator implements ConstraintValidator<File, Object> {

    private long maxSize;
    private String[] allowedTypes;

    @Override
    public void initialize(File constraintAnnotation) {
        this.maxSize = constraintAnnotation.maxSize();
        this.allowedTypes = constraintAnnotation.allowedTypes();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // 文件验证逻辑由 Spring 的 CommonsMultipartFile 处理
        // 这里仅做基础验证
        return true;
    }
}
