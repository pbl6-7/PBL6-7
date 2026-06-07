package com.campus.user.dto;

import com.campus.core.validation.annotation.PhoneNumber;
import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 更新个人资料请求DTO
 * 用于用户更新个人资料时接收请求参数
 */
@Data
public class UpdateProfileRequest {

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空", groups = {UpdateGroup.class})
    @Size(max = 50, message = "真实姓名不能超过50个字符", groups = {UpdateGroup.class})
    private String realName;

    /**
     * 联系方式（手机号）
     */
    @PhoneNumber(message = "手机号格式不正确，应为11位数字且以1开头", groups = {UpdateGroup.class})
    private String contact;
}
