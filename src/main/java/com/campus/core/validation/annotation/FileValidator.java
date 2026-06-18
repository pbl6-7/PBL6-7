package com.campus.core.validation.annotation;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 文件上传验证器
 * 验证上传文件的大小和类型
 */
public class FileValidator implements ConstraintValidator<File, Object> {

    private long maxSize;
    private String[] allowedTypes;

    /** 允许的图片扩展名 */
    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "webp")
    );

    @Override
    public void initialize(File constraintAnnotation) {
        this.maxSize = constraintAnnotation.maxSize();
        this.allowedTypes = constraintAnnotation.allowedTypes();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null值由 @NotNull 注解处理
        }

        if (!(value instanceof MultipartFile)) {
            return true; // 非文件类型跳过校验
        }

        MultipartFile file = (MultipartFile) value;

        // 文件为空由其他校验处理
        if (file.isEmpty()) {
            return true;
        }

        // 文件大小校验
        if (file.getSize() > maxSize) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "文件大小超过限制（最大" + (maxSize / 1024 / 1024) + "MB）"
            ).addConstraintViolation();
            return false;
        }

        // 文件类型校验
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

            // 如果指定了允许类型，则按指定类型校验
            if (allowedTypes != null && allowedTypes.length > 0) {
                Set<String> allowed = new HashSet<>(Arrays.asList(allowedTypes));
                if (!allowed.contains(extension)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                            "不支持的文件类型，允许：" + String.join("、", allowedTypes)
                    ).addConstraintViolation();
                    return false;
                }
            } else {
                // 默认只允许图片
                if (!IMAGE_EXTENSIONS.contains(extension)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                            "只支持图片文件：" + String.join("、", IMAGE_EXTENSIONS)
                    ).addConstraintViolation();
                    return false;
                }
            }
        }

        return true;
    }
}
