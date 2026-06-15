package com.campus.core.common;

import java.util.List;
import java.util.Map;

/**
 * 验证异常
 */
public class ValidationException extends BusinessException {
    
    private final String field;
    private final Object rejectedValue;
    private final Map<String, List<String>> errors;

    public ValidationException(String message) {
        super(ResultCode.VALIDATION_ERROR, message);
        this.field = null;
        this.rejectedValue = null;
        this.errors = null;
    }
    
    public ValidationException(String field, Object rejectedValue) {
        super(ResultCode.VALIDATION_ERROR, "Validation failed for field: " + field);
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.errors = null;
    }
    
    public ValidationException(String field, Object rejectedValue, String message) {
        super(ResultCode.VALIDATION_ERROR, message);
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.errors = null;
    }

    public ValidationException(Map<String, List<String>> errors) {
        super(ResultCode.VALIDATION_ERROR, "Validation failed");
        this.field = null;
        this.rejectedValue = null;
        this.errors = errors;
    }

    public String getField() {
        return field;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }
}
