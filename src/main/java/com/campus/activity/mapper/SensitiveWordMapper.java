package com.campus.activity.mapper;

import com.campus.activity.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 敏感词Mapper接口
 */
@Mapper
public interface SensitiveWordMapper {

    /**
     * 查询所有敏感词
     */
    List<SensitiveWord> selectAll();

    /**
     * 根据ID查询敏感词
     */
    SensitiveWord selectById(@Param("id") Long id);

    /**
     * 根据类型查询敏感词
     */
    List<SensitiveWord> selectByType(@Param("type") String type);

    /**
     * 插入敏感词
     */
    void insert(SensitiveWord sensitiveWord);

    /**
     * 更新敏感词
     */
    void updateById(SensitiveWord sensitiveWord);

    /**
     * 删除敏感词
     */
    void deleteById(@Param("id") Long id);

    /**
     * 批量删除敏感词
     */
    void batchDelete(@Param("ids") List<Long> ids);

    /**
     * 统计敏感词数量
     */
    Long countByType(@Param("type") String type);

    /**
     * 统计所有敏感词数量
     */
    Long countAll();
}
