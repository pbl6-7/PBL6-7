package com.campus.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户导出响应DTO
 * 用于返回用户导出信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserExportResponse {

    /**
     * 导出时间
     */
    private LocalDateTime exportTime;

    /**
     * 管理员ID
     */
    private Long adminId;

    /**
     * 总数
     */
    private int totalCount;

    /**
     * 导出格式
     */
    private String format;

    /**
     * 导出用户数据列表
     */
    private List<UserExportData> users;

    /**
     * 用户导出数据内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserExportData {
        private Long id;
        private String username;
        private String realName;
        private String role;
        private String status;
        private String contact;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}