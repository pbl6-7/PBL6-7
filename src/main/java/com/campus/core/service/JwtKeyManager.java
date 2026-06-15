package com.campus.core.service;

import com.campus.core.entity.JwtKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JWT密钥管理服务类
 * 提供密钥的加载、激活和轮换功能
 */
@Slf4j
@Service
public class JwtKeyManager {

    /**
     * 密钥缓存
     */
    private final ConcurrentHashMap<Integer, JwtKey> keyCache = new ConcurrentHashMap<>();

    /**
     * 当前激活的密钥版本
     */
    private volatile Integer activeVersion = 1;

    /**
     * ID生成器
     */
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * 获取当前激活的密钥
     *
     * @return JwtKey对象
     */
    public JwtKey getActiveKey() {
        JwtKey key = keyCache.get(activeVersion);
        if (key == null) {
            // 如果缓存中没有，创建默认密钥
            key = createDefaultKey();
            keyCache.put(key.getVersion(), key);
        }
        return key;
    }

    /**
     * 获取所有激活的密钥
     *
     * @return 激活密钥列表
     */
    public List<JwtKey> getAllActiveKeys() {
        List<JwtKey> activeKeys = new ArrayList<>();
        for (JwtKey key : keyCache.values()) {
            if (Boolean.TRUE.equals(key.getIsActive())) {
                activeKeys.add(key);
            }
        }
        return activeKeys;
    }

    /**
     * 创建默认密钥
     *
     * @return 默认密钥
     */
    private JwtKey createDefaultKey() {
        JwtKey key = new JwtKey();
        key.setId(idGenerator.getAndIncrement());
        key.setVersion(1);
        key.setKeyValue("defaultSecretKeyForCampusActivityPlatform2024!");
        key.setIsActive(true);
        key.setCreatedAt(LocalDateTime.now());
        key.setActivatedAt(LocalDateTime.now());
        return key;
    }

    /**
     * 获取密钥信息
     *
     * @return 密钥信息字符串
     */
    public String getKeyInfo() {
        JwtKey activeKey = getActiveKey();
        return "JWT密钥管理 - 当前版本: " + activeKey.getVersion() +
               ", 缓存密钥数: " + keyCache.size();
    }

    /**
     * 添加新密钥
     *
     * @param keyValue 密钥值（Base64编码）
     * @param createdBy 创建者ID
     * @return 新密钥
     */
    public JwtKey addKey(String keyValue, Long createdBy) {
        int newVersion = activeVersion + 1;
        JwtKey key = new JwtKey();
        key.setId(idGenerator.getAndIncrement());
        key.setVersion(newVersion);
        key.setKeyValue(keyValue);
        key.setIsActive(false);
        key.setCreatedAt(LocalDateTime.now());
        key.setCreatedBy(createdBy);

        keyCache.put(newVersion, key);
        log.info("添加新密钥，版本: {}", newVersion);

        return key;
    }

    /**
     * 激活密钥
     *
     * @param version 密钥版本
     */
    public void activateKey(int version) {
        JwtKey key = keyCache.get(version);
        if (key == null) {
            log.warn("尝试激活不存在的密钥版本: {}", version);
            return;
        }

        // 停用旧密钥
        JwtKey oldKey = keyCache.get(activeVersion);
        if (oldKey != null) {
            oldKey.setIsActive(false);
        }

        // 激活新密钥
        key.setIsActive(true);
        key.setActivatedAt(LocalDateTime.now());
        activeVersion = version;

        log.info("激活密钥版本: {}", version);
    }
}
