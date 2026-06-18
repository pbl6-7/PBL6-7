package com.campus.core.common;

/**
 * 认证异常
 */
public class AuthenticationException extends BusinessException {
    
    private final String authenticationType;

    public AuthenticationException(String message) {
        super(ResultCode.UNAUTHORIZED, message);
        this.authenticationType = null;
    }
    
    public AuthenticationException(String authenticationType, String message) {
        super(ResultCode.UNAUTHORIZED, message);
        this.authenticationType = authenticationType;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }
}
