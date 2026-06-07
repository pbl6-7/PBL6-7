package com.campus.user.dto;

import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 设置密保请求DTO
 * 用于用户设置密保问题时接收请求参数
 */
@Data
public class SetSecurityRequest {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Min(value = 1, message = "用户ID必须大于0", groups = {CreateGroup.class, UpdateGroup.class})
    private Long userId;

    /**
     * 密保问题ID
     */
    @NotNull(message = "请选择密保问题", groups = {CreateGroup.class, UpdateGroup.class})
    @Min(value = 1, message = "密保问题ID必须大于0", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer securityQuestionId;

    /**
     * 密保答案
     */
    @NotBlank(message = "密保答案不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(min = 2, max = 100, message = "密保答案长度必须在2-100个字符之间", groups = {CreateGroup.class, UpdateGroup.class})
    private String securityAnswer;
}
