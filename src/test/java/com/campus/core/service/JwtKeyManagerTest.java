package com.campus.core.service;

import com.campus.core.entity.JwtKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT密钥管理器单元测试
 */
class JwtKeyManagerTest {

    private JwtKeyManager keyManager;

    @BeforeEach
    void setUp() {
        keyManager = new JwtKeyManager();
    }

    @Test
    void testGetActiveKey_CreatesDefaultKey() {
        JwtKey key = keyManager.getActiveKey();
        assertNotNull(key);
        assertNotNull(key.getKeyValue());
        assertTrue(key.getIsActive());
        assertEquals(1, key.getVersion());
    }

    @Test
    void testDefaultKeyIs256Bit() {
        JwtKey key = keyManager.getActiveKey();
        assertNotNull(key.getKeyValue());
        // Base64编码的256位密钥解码后应为32字节
        byte[] decoded = java.util.Base64.getDecoder().decode(key.getKeyValue());
        assertEquals(32, decoded.length, "密钥应为256位（32字节）");
    }

    @Test
    void testAddKey() {
        String testKey = java.util.Base64.getEncoder().encodeToString(
                new byte[32]); // 256位测试密钥
        keyManager.addKey(testKey, null);
        // 添加后版本应递增
        JwtKey activeKey = keyManager.getActiveKey();
        assertNotNull(activeKey);
    }

    @Test
    void testActivateKey() {
        String testKey = java.util.Base64.getEncoder().encodeToString(
                new byte[32]);
        keyManager.addKey(testKey, null);
        // 激活新版本
        keyManager.activateKey(2);
        JwtKey activeKey = keyManager.getActiveKey();
        assertEquals(2, activeKey.getVersion());
    }

    @Test
    void testCheckAndRotateIfNeeded_NoRotationNeeded() {
        // 新创建的密钥不需要轮换
        keyManager.getActiveKey();
        // 不应抛异常
        assertDoesNotThrow(() -> keyManager.checkAndRotateIfNeeded());
    }

    @Test
    void testKeyValueNotNull() {
        JwtKey key = keyManager.getActiveKey();
        assertNotNull(key.getKeyValue());
        assertFalse(key.getKeyValue().isEmpty());
    }

    @Test
    void testMultipleKeysDifferentVersions() {
        JwtKey key1 = keyManager.getActiveKey();
        int version1 = key1.getVersion();

        String newKey = java.util.Base64.getEncoder().encodeToString(
                new byte[32]);
        keyManager.addKey(newKey, null);
        keyManager.activateKey(version1 + 1);

        JwtKey key2 = keyManager.getActiveKey();
        assertEquals(version1 + 1, key2.getVersion());
        assertNotEquals(key1.getKeyValue(), key2.getKeyValue());
    }
}
