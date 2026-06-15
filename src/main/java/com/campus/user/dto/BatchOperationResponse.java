package com.campus.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 批量操作响应DTO
 * 用于返回批量操作的结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchOperationResponse {

    /**
     * 总操作数
     */
    private int total;

    /**
     * 成功数
     */
    private int success;

    /**
     * 失败数
     */
    private int failed;

    /**
     * 成功的信息
     */
    private String message;

    /**
     * 失败的ID列表（逗号分隔字符串）
     */
    private String failedIds;

    /**
     * 失败的ID列表
     */
    private List<Long> failedIdList;

    /**
     * 失败原因列表
     */
    private List<String> failureReasons;

    /**
     * 便捷构造器（兼容旧用法）
     */
    public static BatchOperationResponse of(int total, int success, int failed, String failedIds, String message) {
        return new BatchOperationResponse(total, success, failed, message, failedIds, null, null);
    }

    /**
     * 便捷构造器（新用法）
     */
    public static BatchOperationResponse of(int success, int failed, List<Long> failedIdList, List<String> failureReasons) {
        String msg = failureReasons == null || failureReasons.isEmpty() ? "操作成功" : String.join("; ", failureReasons);
        String ids = failedIdList == null ? "" : failedIdList.stream().map(String::valueOf).collect(Collectors.joining(","));
        return new BatchOperationResponse(success + failed, success, failed, msg, ids, failedIdList, failureReasons);
    }
}