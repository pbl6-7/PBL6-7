package com.campus.core.mapper;

import com.campus.core.entity.JwtKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * JWT密钥Mapper接口
 * 提供JWT密钥的数据库持久化操作
 */
@Mapper
public interface JwtKeyMapper {

    /**
     * 插入密钥记录
     * @param jwtKey JWT密钥实体
     */
    void insert(JwtKey jwtKey);

    /**
     * 根据版本号查询密钥
     * @param version 密钥版本
     * @return JWT密钥实体
     */
    JwtKey selectByVersion(@Param("version") Integer version);

    /**
     * 查询当前激活的密钥
     * @return 激活的密钥
     */
    JwtKey selectActiveKey();

    /**
     * 查询所有激活的密钥
     * @return 激活的密钥列表
     */
    List<JwtKey> selectAllActiveKeys();

    /**
     * 更新密钥激活状态
     * @param version 密钥版本
     * @param isActive 是否激活
     */
    void updateActiveStatus(@Param("version") Integer version, @Param("isActive") Boolean isActive);

    /**
     * 更新密钥激活时间
     * @param version 密钥版本
     */
    void updateActivatedAt(@Param("version") Integer version);

    /**
     * 删除过期的密钥
     * @param cutoff 截止时间
     */
    void deleteExpiredKeys(@Param("cutoff") java.time.LocalDateTime cutoff);

    /**
     * 查询密钥总数
     * @return 密钥总数
     */
    Long countAll();
}
