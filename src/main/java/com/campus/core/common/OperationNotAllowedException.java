package com.campus.core.common;

/**
 * 操作不允许异常
 */
public class OperationNotAllowedException extends BusinessException {
    
    private final String operation;
    private final String reason;

    public OperationNotAllowedException(String message) {
        super(ResultCode.FORBIDDEN, message);
        this.operation = null;
        this.reason = null;
    }
    
    public OperationNotAllowedException(String operation, String reason) {
        super(ResultCode.FORBIDDEN, "Operation not allowed: " + operation + ". Reason: " + reason);
        this.operation = operation;
        this.reason = reason;
    }
    
    public OperationNotAllowedException(String operation, String reason, String message) {
        super(ResultCode.FORBIDDEN, message);
        this.operation = operation;
        this.reason = reason;
    }

    public String getOperation() {
        return operation;
    }

    public String getReason() {
        return reason;
    }
}
