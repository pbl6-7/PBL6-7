package com.campus.user.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户分页查询请求DTO
 * 用于查询用户列表时接收请求参数
 */
@Data
public class UserPageRequest {

    /**
     * 搜索关键词
     */
    @Size(max = 100, message = "搜索关键词不能超过100个字符")
    private String keyword;

    /**
     * 用户角色
     */
    @Pattern(regexp = "^(USER|ADMIN)$", message = "用户角色只能是USER或ADMIN")
    private String role;

    /**
     * 页码
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer page;

    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer size;
}
