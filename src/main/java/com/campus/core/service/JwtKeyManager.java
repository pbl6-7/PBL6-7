package com.campus.core.service;

import com.campus.core.entity.JwtKey;
import com.campus.core.mapper.JwtKeyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JWT密钥管理服务类
 * 提供密钥的加载、激活和轮换功能
 * 当数据库可用时，密钥会持久化到 jwt_key 表；否则仅使用内存缓存
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
     * ID生成器（仅在无数据库时使用）
     */
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * JWT密钥Mapper（可选依赖）
     */
    @Autowired(required = false)
    private JwtKeyMapper jwtKeyMapper;

    /**
     * 初始化：从数据库加载已有密钥
     */
    @javax.annotation.PostConstruct
    public void init() {
        if (jwtKeyMapper != null) {
            try {
                loadKeysFromDatabase();
                log.info("JWT密钥管理器初始化完成，已从数据库加载密钥");
            } catch (Exception e) {
                log.warn("从数据库加载密钥失败，将使用内存模式: {}", e.getMessage());
            }
        } else {
            log.info("JwtKeyMapper未注入，JWT密钥管理器使用纯内存模式");
        }
    }

    /**
     * 从数据库加载密钥到缓存
     */
    private void loadKeysFromDatabase() {
        if (jwtKeyMapper == null) {
            return;
        }

        try {
            JwtKey activeKey = jwtKeyMapper.selectActiveKey();
            if (activeKey != null) {
                keyCache.put(activeKey.getVersion(), activeKey);
                activeVersion = activeKey.getVersion();
                log.info("从数据库加载激活密钥，版本: {}", activeKey.getVersion());
            }

            List<JwtKey> allActiveKeys = jwtKeyMapper.selectAllActiveKeys();
            for (JwtKey key : allActiveKeys) {
                keyCache.put(key.getVersion(), key);
            }
            log.info("从数据库加载了 {} 个激活密钥", allActiveKeys.size());
        } catch (Exception e) {
            log.error("从数据库加载密钥失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取当前激活的密钥
     *
     * @return JwtKey对象
     */
    public JwtKey getActiveKey() {
        JwtKey key = keyCache.get(activeVersion);
        if (key == null) {
            // 尝试从数据库加载
            if (jwtKeyMapper != null) {
                try {
                    key = jwtKeyMapper.selectActiveKey();
                    if (key != null) {
                        keyCache.put(key.getVersion(), key);
                        activeVersion = key.getVersion();
                        return key;
                    }
                } catch (Exception e) {
                    log.warn("从数据库获取激活密钥失败: {}", e.getMessage());
                }
            }
            // 如果缓存和数据库都没有，创建默认密钥
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

        // 如果缓存为空，尝试从数据库加载
        if (activeKeys.isEmpty() && jwtKeyMapper != null) {
            try {
                activeKeys = jwtKeyMapper.selectAllActiveKeys();
                for (JwtKey key : activeKeys) {
                    keyCache.put(key.getVersion(), key);
                }
            } catch (Exception e) {
                log.warn("从数据库获取激活密钥列表失败: {}", e.getMessage());
            }
        }

        return activeKeys;
    }

    /**
     * 创建默认密钥（动态生成256位强密钥）
     *
     * @return 默认密钥
     */
    private JwtKey createDefaultKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            keyGen.init(256, new SecureRandom());
            SecretKey secretKey = keyGen.generateKey();
            String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

            JwtKey defaultKey = new JwtKey();
            defaultKey.setId(1L);
            defaultKey.setKeyValue(encodedKey);
            defaultKey.setVersion(1);
            defaultKey.setIsActive(true);
            defaultKey.setExpiresAt(LocalDateTime.now().plusYears(1));
            defaultKey.setCreatedAt(LocalDateTime.now());

            keyCache.put(1, defaultKey);
            activeVersion = 1;

            // 持久化到数据库
            persistKey(defaultKey);

            log.info("动态生成256位JWT密钥，版本: 1");
            return defaultKey;
        } catch (NoSuchAlgorithmException e) {
            log.error("生成JWT密钥失败: {}", e.getMessage());
            throw new RuntimeException("无法生成JWT密钥", e);
        }
    }

    /**
     * 获取密钥信息
     *
     * @return 密钥信息字符串
     */
    public String getKeyInfo() {
        JwtKey activeKey = getActiveKey();
        String storageMode = jwtKeyMapper != null ? "数据库+内存" : "仅内存";
        return "JWT密钥管理 - 当前版本: " + activeKey.getVersion() +
               ", 缓存密钥数: " + keyCache.size() +
               ", 存储模式: " + storageMode;
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

        // 持久化到数据库
        persistKey(key);

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
            // 尝试从数据库加载
            if (jwtKeyMapper != null) {
                key = jwtKeyMapper.selectByVersion(version);
                if (key != null) {
                    keyCache.put(version, key);
                }
            }
            if (key == null) {
                log.warn("尝试激活不存在的密钥版本: {}", version);
                return;
            }
        }

        // 停用旧密钥
        JwtKey oldKey = keyCache.get(activeVersion);
        if (oldKey != null) {
            oldKey.setIsActive(false);
            updateKeyActiveStatus(activeVersion, false);
        }

        // 激活新密钥
        key.setIsActive(true);
        key.setActivatedAt(LocalDateTime.now());
        activeVersion = version;

        // 更新数据库
        updateKeyActiveStatus(version, true);

        log.info("激活密钥版本: {}", version);
    }

    /**
     * 检查并执行密钥轮换（如果需要）
     * 当当前活跃密钥使用超过30天时，自动生成新密钥并激活
     */
    public void checkAndRotateIfNeeded() {
        try {
            JwtKey activeKey = getActiveKey();
            if (activeKey == null) {
                log.warn("没有活跃的JWT密钥，创建新密钥");
                createDefaultKey();
                return;
            }

            // 检查密钥是否即将过期（创建超过30天）
            LocalDateTime createdAt = activeKey.getCreatedAt();
            if (createdAt != null && createdAt.isBefore(LocalDateTime.now().minusDays(30))) {
                log.info("JWT密钥已使用超过30天，开始轮换");

                // 生成新密钥
                KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
                keyGen.init(256, new SecureRandom());
                SecretKey secretKey = keyGen.generateKey();
                String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

                JwtKey newKey = addKey(encodedKey, null);
                activateKey(newKey.getVersion());

                log.info("JWT密钥轮换完成，新版本: {}", newKey.getVersion());
            } else {
                log.debug("JWT密钥无需轮换，当前版本: {}", activeKey.getVersion());
            }
        } catch (Exception e) {
            log.error("JWT密钥轮换检查失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 持久化密钥到数据库
     *
     * @param key JWT密钥实体
     */
    private void persistKey(JwtKey key) {
        if (jwtKeyMapper == null) {
            return;
        }
        try {
            jwtKeyMapper.insert(key);
            log.debug("密钥已持久化到数据库，版本: {}", key.getVersion());
        } catch (Exception e) {
            log.warn("密钥持久化到数据库失败（继续使用内存模式）: {}", e.getMessage());
        }
    }

    /**
     * 更新密钥激活状态到数据库
     *
     * @param version 密钥版本
     * @param isActive 是否激活
     */
    private void updateKeyActiveStatus(int version, boolean isActive) {
        if (jwtKeyMapper == null) {
            return;
        }
        try {
            jwtKeyMapper.updateActiveStatus(version, isActive);
            if (isActive) {
                jwtKeyMapper.updateActivatedAt(version);
            }
        } catch (Exception e) {
            log.warn("更新密钥激活状态到数据库失败: {}", e.getMessage());
        }
    }
}
