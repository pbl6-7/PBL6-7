package com.campus.activity.dto;

import com.campus.core.validation.annotation.NoSensitiveWord;
import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 活动审批请求DTO
 * 用于审批活动时接收请求参数
 */
@Data
public class ActivityApprovalRequest {

    /**
     * 审批理由（可选）
     */
    @Size(max = 500, message = "审批理由不能超过500个字符", groups = {UpdateGroup.class})
    @NoSensitiveWord(message = "审批理由包含敏感词，请修改后重新提交", groups = {UpdateGroup.class})
    private String reason;
}
