package com.campus.activity.controller;

import com.campus.activity.dto.ActivityTagRequest;
import com.campus.activity.dto.TagCreateRequest;
import com.campus.activity.dto.TagResponse;
import com.campus.activity.service.ActivityTagService;
import com.campus.activity.service.TagService;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.core.constants.UserRoleConstants;
import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Api(tags = "活动标签管理")
public class TagController {

    private final TagService tagService;
    private final ActivityTagService activityTagService;

    /**
     * 创建标签
     * 仅允许管理员创建标签
     */
    @PostMapping
    @ApiOperation("创建标签")
    public Result<TagResponse> createTag(
            HttpServletRequest request,
            @Validated({CreateGroup.class}) @RequestBody TagCreateRequest requestObj) {
        String currentUserRole = (String) request.getAttribute("currentUserRole");
        if (currentUserRole == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        if (!UserRoleConstants.ADMIN.equals(currentUserRole)) {
            return Result.error(ResultCode.FORBIDDEN, "只有管理员才能创建标签");
        }

        TagResponse response = tagService.createTag(requestObj);
        return Result.success(response, "标签创建成功");
    }

    @GetMapping
    @ApiOperation("获取所有标签")
    public Result<List<TagResponse>> getAllTags() {
        List<TagResponse> tags = tagService.getAllTags();
        return Result.success(tags);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取标签详情")
    public Result<TagResponse> getTagById(@PathVariable Long id) {
        TagResponse response = tagService.getTagById(id);
        return Result.success(response);
    }

    /**
     * 删除标签
     * 仅允许管理员删除标签
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除标签")
    public Result<Void> deleteTag(
            HttpServletRequest request,
            @PathVariable Long id) {
        String currentUserRole = (String) request.getAttribute("currentUserRole");
        if (currentUserRole == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        if (!UserRoleConstants.ADMIN.equals(currentUserRole)) {
            return Result.error(ResultCode.FORBIDDEN, "只有管理员才能删除标签");
        }

        tagService.deleteTag(id);
        return Result.success(null, "标签删除成功");
    }

    /**
     * 更新标签
     * 仅允许管理员更新标签
     */
    @PutMapping("/{id}")
    @ApiOperation("更新标签")
    public Result<TagResponse> updateTag(
            HttpServletRequest request,
            @PathVariable Long id,
            @Validated({UpdateGroup.class}) @RequestBody TagCreateRequest requestObj) {
        String currentUserRole = (String) request.getAttribute("currentUserRole");
        if (currentUserRole == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        if (!UserRoleConstants.ADMIN.equals(currentUserRole)) {
            return Result.error(ResultCode.FORBIDDEN, "只有管理员才能更新标签");
        }

        TagResponse response = tagService.updateTag(id, requestObj);
        return Result.success(response, "标签更新成功");
    }

    @GetMapping("/activity/{activityId}")
    @ApiOperation("获取活动的标签")
    public Result<List<TagResponse>> getTagsByActivityId(@PathVariable Long activityId) {
        List<TagResponse> tags = activityTagService.getTagsByActivityId(activityId);
        return Result.success(tags);
    }

    /**
     * 为活动设置标签
     * 添加权限验证
     */
    @PostMapping("/activity")
    @ApiOperation("为活动设置标签")
    public Result<Void> setActivityTags(
            HttpServletRequest request,
            @Validated({UpdateGroup.class}) @RequestBody ActivityTagRequest requestObj) {
        Long userId = (Long) request.getAttribute("currentUserId");
        String role = (String) request.getAttribute("currentUserRole");
        if (userId == null || role == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        
        activityTagService.setActivityTags(requestObj.getActivityId(), requestObj.getTagIds(), userId, role);
        return Result.success(null, "活动标签设置成功");
    }
}
