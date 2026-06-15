package com.campus.core.common;

import java.util.List;

/**
 * 授权异常
 */
public class AuthorizationException extends BusinessException {
    
    private final String requiredPermission;
    private final List<String> currentPermissions;

    public AuthorizationException(String message) {
        super(ResultCode.FORBIDDEN, message);
        this.requiredPermission = null;
        this.currentPermissions = null;
    }
    
    public AuthorizationException(String requiredPermission, List<String> currentPermissions) {
        super(ResultCode.FORBIDDEN, "Insufficient permissions. Required: " + requiredPermission);
        this.requiredPermission = requiredPermission;
        this.currentPermissions = currentPermissions;
    }
    
    public AuthorizationException(String requiredPermission, List<String> currentPermissions, String message) {
        super(ResultCode.FORBIDDEN, message);
        this.requiredPermission = requiredPermission;
        this.currentPermissions = currentPermissions;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public List<String> getCurrentPermissions() {
        return currentPermissions;
    }
}
