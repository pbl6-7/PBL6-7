package com.campus.user.dto;

import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * 更新用户角色请求DTO
 * 用于管理员更新用户角色时接收请求参数
 */
@Data
public class UpdateRoleRequest {

    /**
     * 用户角色
     */
    @Pattern(regexp = "^(USER|ADMIN)$", message = "用户角色只能是USER或ADMIN", groups = {UpdateGroup.class})
    private String role;
}
