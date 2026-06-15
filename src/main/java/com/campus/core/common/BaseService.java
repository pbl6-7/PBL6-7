package com.campus.core.common;

import lombok.Data;

/**
 * 基础服务类
 * 提供通用的服务方法
 */
@Data
public abstract class BaseService<T> {

    /**
     * 获取当前实体类型
     */
    protected abstract Class<T> getEntityClass();

    /**
     * 验证ID
     */
    protected boolean isValidId(Long id) {
        return id != null && id > 0;
    }

    /**
     * 验证对象不为空
     */
    protected void validateNotNull(Object obj, ResultCode code, String message) {
        if (obj == null) {
            throw new BusinessException(code, message);
        }
    }

    /**
     * 验证字符串不为空
     */
    protected void validateNotEmpty(String str, ResultCode code, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new BusinessException(code, message);
        }
    }
}
