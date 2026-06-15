package com.campus.core.common;

/**
 * 资源不存在异常
 */
public class ResourceNotFoundException extends BusinessException {
    
    private final String resourceType;
    private final Object resourceId;

    public ResourceNotFoundException(String message) {
        super(ResultCode.NOT_FOUND, message);
        this.resourceType = null;
        this.resourceId = null;
    }
    
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(ResultCode.NOT_FOUND, resourceType + " not found with id: " + resourceId);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public ResourceNotFoundException(String resourceType, Object resourceId, String message) {
        super(ResultCode.NOT_FOUND, message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Object getResourceId() {
        return resourceId;
    }
}
