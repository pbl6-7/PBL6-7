package com.campus.activity.controller;

import com.campus.activity.dto.TagResponse;
import com.campus.activity.service.ActivityTagService;
import com.campus.core.common.BusinessException;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Api(tags = "活动标签管理")
public class TagController {

    private static final String ROLE_ADMIN = "admin";

    private final ActivityTagService activityTagService;
    private final JwtUtils jwtUtils;

    /**
     * 创建标签
     * 修复问题2：仅允许管理员创建标签
     */
    @PostMapping
    @ApiOperation("创建标签")
    public Result<TagResponse> createTag(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody TagCreateRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }

        String role = jwtUtils.getRoleFromToken(token);
        if (!ROLE_ADMIN.equals(role)) {
            return Result.error(ResultCode.FORBIDDEN, "只有管理员才能创建标签");
        }

        TagResponse response = activityTagService.createTag(request);
        return Result.success(response, "标签创建成功");
    }

    @GetMapping
    @ApiOperation("获取所有标签")
    public Result<List<TagResponse>> getAllTags() {
        List<TagResponse> tags = activityTagService.getAllTags();
        return Result.success(tags);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取标签详情")
    public Result<TagResponse> getTagById(@PathVariable Long id) {
        TagResponse response = activityTagService.getTagById(id);
        return Result.success(response);
    }

    /**
     * 删除标签
     * 修复问题2：仅允许管理员删除标签
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除标签")
    public Result<Void> deleteTag(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }

        String role = jwtUtils.getRoleFromToken(token);
        if (!ROLE_ADMIN.equals(role)) {
            return Result.error(ResultCode.FORBIDDEN, "只有管理员才能删除标签");
        }

        activityTagService.deleteTag(id);
        return Result.success(null, "标签删除成功");
    }

    @GetMapping("/activity/{activityId}")
    @ApiOperation("获取活动的标签")
    public Result<List<TagResponse>> getTagsByActivityId(@PathVariable Long activityId) {
        List<TagResponse> tags = activityTagService.getTagsByActivityId(activityId);
        return Result.success(tags);
    }

    /**
     * 为活动设置标签
     * 修复问题2：添加权限验证
     */
    @PostMapping("/activity")
    @ApiOperation("为活动设置标签")
    public Result<Void> setActivityTags(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ActivityTagRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        String role = jwtUtils.getRoleFromToken(token);
        if (userId == null || role == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        
        activityTagService.setActivityTags(request.getActivityId(), request.getTagIds(), userId, role);
        return Result.success(null, "活动标签设置成功");
    }
}
