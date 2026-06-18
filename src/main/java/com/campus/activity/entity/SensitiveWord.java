package com.campus.activity.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 敏感词实体类
 * 用于存储敏感词库
 */
@Data
public class SensitiveWord {

    /**
     * 敏感词ID
     */
    private Long id;

    /**
     * 敏感词
     */
    private String word;

    /**
     * 是否为白名单（1-是，0-否）
     */
    private Integer isWhitelist;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 敏感词类型（politics-政治，violence-暴力，porn-色情，other-其他）
     */
    private String type;
}
