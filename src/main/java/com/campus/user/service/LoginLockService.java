package com.campus.user.service;

import com.campus.user.entity.LoginLock;
import com.campus.user.mapper.LoginLockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录锁定服务类
 * 提供登录失败次数统计和账户锁定功能，数据持久化到数据库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLockService {

    private final LoginLockMapper loginLockMapper;

    @Value("${login-lock.max-failures:5}")
    private int maxFailures;

    @Value("${login-lock.lock-duration-minutes:15}")
    private int lockDurationMinutes;

    /**
     * 检查用户是否被锁定
     *
     * @param username 用户名
     * @return 是否被锁定
     */
    public boolean isUserLocked(String username) {
        LoginLock lock = loginLockMapper.selectActiveByUsername(username);
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
    @Transactional(rollbackFor = Exception.class)
    public int recordLoginFailure(String username) {
        // 先查询当前失败次数
        Integer currentCount = loginLockMapper.selectFailCount(username);
        int failCount = (currentCount != null ? currentCount : 0) + 1;

        // 更新失败次数到数据库
        loginLockMapper.upsertFailCount(username, failCount);
        log.info("用户 {} 登录失败，失败次数: {}", username, failCount);
        return failCount;
    }

    /**
     * 获取最大登录失败次数
     *
     * @return 最大失败次数
     */
    public int getMaxLoginFailCount() {
        return maxFailures;
    }

    /**
     * 锁定用户
     *
     * @param username 用户名
     */
    @Transactional(rollbackFor = Exception.class)
    public void lockUser(String username) {
        Integer failCount = loginLockMapper.selectFailCount(username);

        LoginLock lock = new LoginLock();
        lock.setUsername(username);
        lock.setLockTime(LocalDateTime.now());
        lock.setUnlockTime(LocalDateTime.now().plusMinutes(lockDurationMinutes));
        lock.setFailCount(failCount != null ? failCount : maxFailures);
        lock.setLockReason("登录失败次数过多");
        lock.setIsLocked(true);
        lock.setCreatedAt(LocalDateTime.now());
        lock.setUpdatedAt(LocalDateTime.now());

        loginLockMapper.insert(lock);
        log.info("用户 {} 被锁定，解锁时间: {}", username, lock.getUnlockTime());
    }

    /**
     * 解锁用户
     *
     * @param username 用户名
     */
    @Transactional(rollbackFor = Exception.class)
    public void unlockUser(String username) {
        loginLockMapper.unlockByUsername(username);
        log.info("用户 {} 已解锁", username);
    }

    /**
     * 清除登录失败记录
     *
     * @param username 用户名
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearLoginFailure(String username) {
        loginLockMapper.unlockByUsername(username);
        log.debug("清除用户 {} 的登录失败记录", username);
    }

    /**
     * 获取用户登录失败次数
     *
     * @param username 用户名
     * @return 失败次数
     */
    public int getLoginFailCount(String username) {
        Integer count = loginLockMapper.selectFailCount(username);
        return count != null ? count : 0;
    }

    /**
     * 获取所有锁定用户列表
     */
    public List<Map<String, Object>> getLockedList() {
        List<LoginLock> activeLocks = loginLockMapper.selectAllActive();
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDateTime currentTime = LocalDateTime.now();

        for (LoginLock record : activeLocks) {
            Map<String, Object> item = new HashMap<>();
            item.put("username", record.getUsername());
            item.put("lockTime", record.getLockTime());
            item.put("unlockTime", record.getUnlockTime());
            item.put("failCount", record.getFailCount());
            item.put("lockReason", record.getLockReason());
            if (record.getUnlockTime() != null) {
                long remainingMinutes = java.time.Duration.between(currentTime, record.getUnlockTime()).toMinutes();
                item.put("remainingMinutes", Math.max(0, remainingMinutes));
            }
            result.add(item);
        }
        return result;
    }
}
