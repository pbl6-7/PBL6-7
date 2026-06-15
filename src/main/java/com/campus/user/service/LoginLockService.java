package com.campus.user.service;

import com.campus.user.entity.LoginLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录锁定服务类
 * 提供登录失败次数统计和账户锁定功能
 */
@Slf4j
@Service
public class LoginLockService {

    /**
     * 最大登录失败次数
     */
    private static final int MAX_LOGIN_FAIL_COUNT = 5;

    /**
     * 锁定时间（分钟）
     */
    private static final int LOCK_TIME_MINUTES = 15;

    /**
     * 登录失败记录存储（使用 ConcurrentHashMap 模拟数据库）
     * key: username, value: failCount
     */
    private final ConcurrentHashMap<String, Integer> loginFailures = new ConcurrentHashMap<>();

    /**
     * 锁定记录存储（使用 ConcurrentHashMap 模拟数据库）
     * key: username, value: LoginLock
     */
    private final ConcurrentHashMap<String, LoginLock> lockRecords = new ConcurrentHashMap<>();

    /**
     * 检查用户是否被锁定
     *
     * @param username 用户名
     * @return 是否被锁定
     */
    public boolean isUserLocked(String username) {
        LoginLock lock = lockRecords.get(username);
        if (lock == null) {
            return false;
        }

        // 检查锁定是否已过期
        if (lock.getUnlockTime() != null && lock.getUnlockTime().isBefore(LocalDateTime.now())) {
            // 锁定已过期，自动解锁
            unlockUser(username);
            return false;
        }

        return lock.getIsLocked() != null && lock.getIsLocked();
    }

    /**
     * 记录登录失败
     *
     * @param username 用户名
     * @return 当前失败次数
     */
    public int recordLoginFailure(String username) {
        int failCount = loginFailures.merge(username, 1, Integer::sum);
        log.info("用户 {} 登录失败，失败次数: {}", username, failCount);
        return failCount;
    }

    /**
     * 获取最大登录失败次数
     *
     * @return 最大失败次数
     */
    public int getMaxLoginFailCount() {
        return MAX_LOGIN_FAIL_COUNT;
    }

    /**
     * 锁定用户
     *
     * @param username 用户名
     */
    public void lockUser(String username) {
        LoginLock lock = new LoginLock();
        lock.setUsername(username);
        lock.setLockTime(LocalDateTime.now());
        lock.setUnlockTime(LocalDateTime.now().plusMinutes(LOCK_TIME_MINUTES));
        lock.setFailCount(loginFailures.getOrDefault(username, 0));
        lock.setLockReason("登录失败次数过多");
        lock.setIsLocked(true);
        lock.setCreatedAt(LocalDateTime.now());
        lock.setUpdatedAt(LocalDateTime.now());

        lockRecords.put(username, lock);
        log.info("用户 {} 被锁定，解锁时间: {}", username, lock.getUnlockTime());
    }

    /**
     * 解锁用户
     *
     * @param username 用户名
     */
    public void unlockUser(String username) {
        lockRecords.remove(username);
        loginFailures.remove(username);
        log.info("用户 {} 已解锁", username);
    }

    /**
     * 清除登录失败记录
     *
     * @param username 用户名
     */
    public void clearLoginFailure(String username) {
        loginFailures.remove(username);
        log.debug("清除用户 {} 的登录失败记录", username);
    }

    /**
     * 获取用户登录失败次数
     *
     * @param username 用户名
     * @return 失败次数
     */
    public int getLoginFailCount(String username) {
        return loginFailures.getOrDefault(username, 0);
    }
}
