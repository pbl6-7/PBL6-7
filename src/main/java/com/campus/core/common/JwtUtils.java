package com.campus.core.common;

import com.campus.core.entity.JwtKey;
import com.campus.core.service.JwtKeyManager;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * JWT工具类
 * 支持多密钥版本验证和密钥管理
 */
@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expirationTime;

    @Value("${jwt.key.management-enabled:true}")
    private boolean keyManagementEnabled;

    @Autowired(required = false)
    private JwtKeyManager jwtKeyManager;

    /**
     * 密钥缓存，用于快速验证
     */
    private volatile SecretKey currentSigningKey;

    /**
     * 多版本密钥缓存
     */
    private volatile Map<Integer, SecretKey> keyVersionMap = new HashMap<>();

    /**
     * 初始化密钥
     */
    @PostConstruct
    public void init() {
        if (keyManagementEnabled && jwtKeyManager != null) {
            logger.info("JWT密钥管理已启用，使用数据库密钥");
            loadKeysFromDatabase();
        } else {
            logger.info("JWT密钥管理未启用，使用配置文件密钥");
            validateAndSetKey(secretKey);
        }
    }

    /**
     * 从数据库加载密钥
     */
    private void loadKeysFromDatabase() {
        try {
            // 加载当前激活的密钥
            JwtKey activeKey = jwtKeyManager.getActiveKey();
            if (activeKey != null) {
                currentSigningKey = decodeKey(activeKey.getKeyValue());
                logger.info("加载当前激活密钥，版本: {}", activeKey.getVersion());
            }

            // 加载所有激活的密钥（用于验证旧Token）
            List<JwtKey> activeKeys = jwtKeyManager.getAllActiveKeys();
            Map<Integer, SecretKey> newKeyMap = new HashMap<>();
            for (JwtKey key : activeKeys) {
                newKeyMap.put(key.getVersion(), decodeKey(key.getKeyValue()));
            }
            keyVersionMap = newKeyMap;
            logger.info("加载了{}个激活的密钥版本", activeKeys.size());

        } catch (Exception e) {
            logger.error("从数据库加载密钥失败，使用配置文件密钥", e);
            validateAndSetKey(secretKey);
        }
    }

    /**
     * 验证并设置密钥
     * @param key 密钥字符串
     */
    private void validateAndSetKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT密钥长度不足，至少需要32字节（256位），当前长度：" + keyBytes.length);
        }
        currentSigningKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 解码Base64密钥
     * @param base64Key Base64编码的密钥
     * @return SecretKey对象
     */
    private SecretKey decodeKey(String base64Key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            if (keyBytes.length < 32) {
                throw new IllegalStateException("JWT密钥长度不足，至少需要32字节（256位），当前长度：" + keyBytes.length);
            }
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT密钥格式错误，无法解码Base64", e);
        }
    }

    /**
     * 获取签名密钥
     * @return 当前签名密钥
     */
    private SecretKey getSigningKey() {
        if (keyManagementEnabled && jwtKeyManager != null) {
            // 刷新密钥缓存
            refreshKeysIfNeeded();
            if (currentSigningKey == null) {
                loadKeysFromDatabase();
            }
        }
        return currentSigningKey;
    }

    /**
     * 刷新密钥缓存（如果需要）
     */
    private void refreshKeysIfNeeded() {
        if (jwtKeyManager == null) {
            return;
        }
        try {
            JwtKey activeKey = jwtKeyManager.getActiveKey();
            if (activeKey != null && (currentSigningKey == null || keyVersionMap.isEmpty())) {
                loadKeysFromDatabase();
            }
        } catch (Exception e) {
            logger.error("刷新密钥缓存失败", e);
        }
    }

    /**
     * 生成JWT Token
     * @param userId 用户ID
     * @param username 用户名
     * @param role 角色
     * @return JWT Token字符串
     */
    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        // 如果启用了密钥管理，添加密钥版本
        if (keyManagementEnabled && jwtKeyManager != null) {
            JwtKey activeKey = jwtKeyManager.getActiveKey();
            if (activeKey != null) {
                claims.put("keyVersion", activeKey.getVersion());
            }
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析Token获取用户名
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 解析Token获取用户ID
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 解析Token获取角色
     * @param token JWT Token
     * @return 角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 验证Token是否有效
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (JwtException e) {
            logger.debug("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证Token是否过期
     * @param token JWT Token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * 刷新Token
     * @param token 旧Token
     * @return 新Token
     */
    public String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(claims.getSubject())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从Token中解析Claims
     * 支持多密钥版本验证
     * @param token JWT Token
     * @return Claims对象
     */
    private Claims getClaimsFromToken(String token) {
        // 如果启用了密钥管理，尝试使用多版本密钥验证
        if (keyManagementEnabled && jwtKeyManager != null) {
            return getClaimsFromTokenWithMultiKey(token);
        }

        // 使用单一密钥验证
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 使用多版本密钥验证Token
     * @param token JWT Token
     * @return Claims对象
     */
    private Claims getClaimsFromTokenWithMultiKey(String token) {
        // 先尝试不解析获取密钥版本
        Integer keyVersion = tryExtractKeyVersion(token);

        // 如果有密钥版本，使用对应版本的密钥
        if (keyVersion != null) {
            SecretKey key = keyVersionMap.get(keyVersion);
            if (key != null) {
                try {
                    return Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
                } catch (JwtException e) {
                    logger.debug("使用密钥版本{}验证失败: {}", keyVersion, e.getMessage());
                }
            }
        }

        // 尝试所有激活的密钥
        for (Map.Entry<Integer, SecretKey> entry : keyVersionMap.entrySet()) {
            try {
                return Jwts.parserBuilder()
                        .setSigningKey(entry.getValue())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
            } catch (JwtException e) {
                // 继续尝试下一个密钥
            }
        }

        // 如果都失败了，尝试当前密钥
        if (currentSigningKey != null) {
            return Jwts.parserBuilder()
                    .setSigningKey(currentSigningKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }

        throw new JwtException("无法验证Token：没有匹配的密钥");
    }

    /**
     * 尝试从Token中提取密钥版本（不验证签名）
     * @param token JWT Token
     * @return 密钥版本，如果不存在则返回null
     */
    private Integer tryExtractKeyVersion(String token) {
        try {
            // 分割Token
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            // 解码Payload
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            
            // 简单解析JSON查找keyVersion
            if (payload.contains("\"keyVersion\"")) {
                int start = payload.indexOf("\"keyVersion\":") + "\"keyVersion\":".length();
                int end = payload.indexOf(",", start);
                if (end == -1) {
                    end = payload.indexOf("}", start);
                }
                if (start > 0 && end > start) {
                    return Integer.parseInt(payload.substring(start, end).trim());
                }
            }
        } catch (Exception e) {
            // 解析失败，返回null
        }
        return null;
    }

    /**
     * 重新加载密钥
     * 用于密钥轮换后刷新缓存
     */
    public void reloadKeys() {
        if (keyManagementEnabled && jwtKeyManager != null) {
            loadKeysFromDatabase();
            logger.info("JWT密钥已重新加载");
        }
    }

    /**
     * 获取当前密钥信息
     * @return 密钥信息
     */
    public String getCurrentKeyInfo() {
        if (keyManagementEnabled && jwtKeyManager != null) {
            return jwtKeyManager.getKeyInfo();
        }
        return "使用配置文件密钥，长度: " + secretKey.length() + "字符";
    }
}