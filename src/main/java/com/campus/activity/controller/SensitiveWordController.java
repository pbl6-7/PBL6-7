package com.campus.activity.controller;

import com.campus.activity.entity.SensitiveWord;
import com.campus.activity.service.SensitiveWordService;
import com.campus.core.common.BusinessException;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.core.constants.UserRoleConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 敏感词管理控制器
 * 仅管理员可操作
 */
@RestController
@RequestMapping("/api/admin/sensitive-words")
@RequiredArgsConstructor
@Api(tags = "管理员-敏感词管理")
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    /**
     * 验证管理员权限
     */
    private void validateAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        String role = (String) request.getAttribute("currentUserRole");
        if (userId == null || role == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        if (!UserRoleConstants.ADMIN.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "需要管理员权限");
        }
    }

    /**
     * 获取所有敏感词
     */
    @GetMapping
    @ApiOperation("获取所有敏感词")
    public Result<List<SensitiveWord>> getAllSensitiveWords(HttpServletRequest request) {
        validateAdmin(request);
        List<SensitiveWord> words = sensitiveWordService.getAllSensitiveWords();
        return Result.success(words);
    }

    /**
     * 根据类型获取敏感词
     */
    @GetMapping("/type/{type}")
    @ApiOperation("根据类型获取敏感词")
    public Result<List<SensitiveWord>> getSensitiveWordsByType(
            HttpServletRequest request,
            @PathVariable String type) {
        validateAdmin(request);
        List<SensitiveWord> words = sensitiveWordService.getSensitiveWordsByType(type);
        return Result.success(words);
    }

    /**
     * 获取敏感词详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取敏感词详情")
    public Result<SensitiveWord> getSensitiveWordById(
            HttpServletRequest request,
            @PathVariable Long id) {
        validateAdmin(request);
        SensitiveWord word = sensitiveWordService.getSensitiveWordById(id);
        if (word == null) {
            return Result.error(ResultCode.NOT_FOUND, "敏感词不存在");
        }
        return Result.success(word);
    }

    /**
     * 创建敏感词
     */
    @PostMapping
    @ApiOperation("创建敏感词")
    public Result<SensitiveWord> createSensitiveWord(
            HttpServletRequest request,
            @RequestBody SensitiveWord sensitiveWord) {
        validateAdmin(request);
        SensitiveWord created = sensitiveWordService.createSensitiveWord(sensitiveWord);
        return Result.success(created, "敏感词创建成功");
    }

    /**
     * 更新敏感词
     */
    @PutMapping("/{id}")
    @ApiOperation("更新敏感词")
    public Result<SensitiveWord> updateSensitiveWord(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody SensitiveWord sensitiveWord) {
        validateAdmin(request);
        SensitiveWord updated = sensitiveWordService.updateSensitiveWord(id, sensitiveWord);
        return Result.success(updated, "敏感词更新成功");
    }

    /**
     * 删除敏感词
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除敏感词")
    public Result<Void> deleteSensitiveWord(
            HttpServletRequest request,
            @PathVariable Long id) {
        validateAdmin(request);
        sensitiveWordService.deleteSensitiveWord(id);
        return Result.success(null, "敏感词删除成功");
    }

    /**
     * 批量删除敏感词
     */
    @DeleteMapping("/batch")
    @ApiOperation("批量删除敏感词")
    public Result<Void> batchDeleteSensitiveWords(
            HttpServletRequest request,
            @RequestBody List<Long> ids) {
        validateAdmin(request);
        sensitiveWordService.batchDeleteSensitiveWords(ids);
        return Result.success(null, "批量删除成功");
    }

    /**
     * 获取敏感词统计
     */
    @GetMapping("/stats")
    @ApiOperation("获取敏感词统计")
    public Result<Map<String, Object>> getSensitiveWordStats(HttpServletRequest request) {
        validateAdmin(request);
        Map<String, Object> stats = sensitiveWordService.getSensitiveWordStats();
        return Result.success(stats);
    }

    /**
     * 刷新敏感词DFA树
     */
    @PostMapping("/refresh")
    @ApiOperation("刷新敏感词DFA树")
    public Result<Void> refreshSensitiveWordTree(HttpServletRequest request) {
        validateAdmin(request);
        sensitiveWordService.refreshSensitiveWordTree();
        return Result.success(null, "敏感词树刷新成功");
    }

    /**
     * 检查文本是否包含敏感词（公开接口）
     */
    @PostMapping("/check")
    @ApiOperation("检查文本是否包含敏感词")
    public Result<Map<String, Object>> checkSensitiveWord(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        boolean contains = sensitiveWordService.containsSensitiveWord(text);
        String filtered = sensitiveWordService.filterSensitiveWords(text);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("contains", contains);
        result.put("filtered", filtered);
        return Result.success(result);
    }
}
