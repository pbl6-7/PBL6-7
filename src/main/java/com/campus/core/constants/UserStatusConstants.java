package com.campus.core.constants;

/**
 * 用户状态常量
 */
public class UserStatusConstants {

    /**
     * 启用状态
     */
    public static final String ENABLED = "enabled";

    /**
     * 禁用状态
     */
    public static final String DISABLED = "disabled";

    /**
     * 锁定状态
     */
    public static final String LOCKED = "locked";

    /**
     * 待激活状态
     */
    public static final String PENDING = "pending";

    /**
     * 用户禁用状态码
     */
    public static final int USER_DISABLE = 403;

    /**
     * 用户删除状态码
     */
    public static final int USER_DELETE = 410;

    /**
     * 数据导出状态码
     */
    public static final int DATA_EXPORT = 200;

    /**
     * 检查是否禁用
     */
    public static boolean isDisabled(String status) {
        return DISABLED.equals(status);
    }

    /**
     * 检查是否启用
     */
    public static boolean isEnabled(String status) {
        return ENABLED.equals(status);
    }
}
