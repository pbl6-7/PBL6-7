package com.campus.core.validation.annotation;

import com.campus.core.common.SensitiveWordFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 无敏感词验证器
 * 验证文本内容不包含敏感词
 */
@Component
public class NoSensitiveWordValidator implements ConstraintValidator<NoSensitiveWord, String> {

    private SensitiveWordFilter sensitiveWordFilter;

    /**
     * 通过 ApplicationContext 获取 SensitiveWordFilter bean
     */
    @Override
    public void initialize(NoSensitiveWord constraintAnnotation) {
        ApplicationContext context = ApplicationContextProvider.getContext();
        if (context != null) {
            sensitiveWordFilter = context.getBean(SensitiveWordFilter.class);
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        if (sensitiveWordFilter == null) {
            return true; // 如果过滤器未初始化，跳过验证
        }
        return !sensitiveWordFilter.containsSensitiveWord(value);
    }
}
