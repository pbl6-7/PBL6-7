package com.campus.activity.dto;

import lombok.Data;

/**
 * 标签创建请求DTO
 * 用于创建新标签的请求数据
 */
@Data
public class TagCreateRequest {
    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签颜色（十六进制颜色码）
     */
    private String color;
}
