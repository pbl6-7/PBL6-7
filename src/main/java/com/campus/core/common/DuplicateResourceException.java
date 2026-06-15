package com.campus.core.common;

/**
 * 重复资源异常
 */
public class DuplicateResourceException extends BusinessException {
    
    private final String conflictField;
    private final Object conflictValue;

    public DuplicateResourceException(String message) {
        super(ResultCode.BAD_REQUEST, message);
        this.conflictField = null;
        this.conflictValue = null;
    }
    
    public DuplicateResourceException(String conflictField, Object conflictValue) {
        super(ResultCode.BAD_REQUEST, "Duplicate resource: " + conflictField + " = " + conflictValue);
        this.conflictField = conflictField;
        this.conflictValue = conflictValue;
    }
    
    public DuplicateResourceException(String conflictField, Object conflictValue, String message) {
        super(ResultCode.BAD_REQUEST, message);
        this.conflictField = conflictField;
        this.conflictValue = conflictValue;
    }

    public String getConflictField() {
        return conflictField;
    }

    public Object getConflictValue() {
        return conflictValue;
    }
}
