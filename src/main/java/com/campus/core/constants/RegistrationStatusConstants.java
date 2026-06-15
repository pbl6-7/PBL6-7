package com.campus.core.constants;

/**
 * 报名状态常量
 */
public class RegistrationStatusConstants {

    /**
     * 待审核
     */
    public static final String PENDING = "pending";

    /**
     * 已通过
     */
    public static final String APPROVED = "approved";

    /**
     * 已拒绝
     */
    public static final String REJECTED = "rejected";

    /**
     * 已取消
     */
    public static final String CANCELLED = "cancelled";

    /**
     * 已确认
     */
    public static final String CONFIRMED = "confirmed";

    /**
     * 获取状态描述
     */
    public static String getDescription(String status) {
        switch (status) {
            case PENDING: return "待审核";
            case APPROVED: return "已通过";
            case REJECTED: return "已拒绝";
            case CANCELLED: return "已取消";
            case CONFIRMED: return "已确认";
            default: return "未知状态";
        }
    }
}
