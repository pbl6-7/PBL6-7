package com.campus.core.validation.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

/**
 * 有效活动时间验证器
 * 验证活动开始时间必须早于结束时间
 */
public class ValidActivityTimeValidator implements ConstraintValidator<ValidActivityTime, Object> {

    @Override
    public void initialize(ValidActivityTime constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        try {
            java.lang.reflect.Field startTimeField = value.getClass().getDeclaredField("startTime");
            java.lang.reflect.Field endTimeField = value.getClass().getDeclaredField("endTime");
            startTimeField.setAccessible(true);
            endTimeField.setAccessible(true);

            LocalDateTime startTime = (LocalDateTime) startTimeField.get(value);
            LocalDateTime endTime = (LocalDateTime) endTimeField.get(value);

            if (startTime == null || endTime == null) {
                return true; // 让 @NotNull 处理空验证
            }

            return startTime.isBefore(endTime);
        } catch (Exception e) {
            return true; // 如果反射失败，让其他验证器处理
        }
    }
}
