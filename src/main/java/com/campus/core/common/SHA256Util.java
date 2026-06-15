package com.campus.core.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA256加密工具类
 * 提供SHA256加密功能
 */
public class SHA256Util {

    /**
     * 私有构造函数，防止实例化
     */
    private SHA256Util() {
    }

    /**
     * 对字符串进行SHA256加密
     *
     * @param rawStr 原始字符串
     * @return 加密后的十六进制字符串
     */
    public static String encrypt(String rawStr) {
        if (rawStr == null) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawStr.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
