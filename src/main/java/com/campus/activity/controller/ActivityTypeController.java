package com.campus.activity.controller;

import com.campus.activity.dto.ActivityTypeCreateRequest;
import com.campus.activity.dto.ActivityTypeResponse;
import com.campus.activity.service.ActivityTypeService;
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
@RequestMapping("/api/v1/activity-types")
@RequiredArgsConstructor
@Api(tags = "活动类型管理")
public class ActivityTypeController {

    private final ActivityTypeService activityTypeService;

    /**
     * 创建活动类型
     * 仅允许管理员创建活动类型
     */
    @PostMapping
    @ApiOperation("创建活动类型")
    public Result<ActivityTypeResponse> createType(
            HttpServletRequest request,
            @Validated({CreateGroup.class}) @RequestBody ActivityTypeCreateRequest requestObj) {
        String currentUserRole = (String) request.getAttribute("currentUserRole");
        if (currentUserRole == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        if (!UserRoleConstants.ADMIN.equals(currentUserRole)) {
            return Result.error(ResultCode.FORBIDDEN, "只有管理员才能创建活动类型");
        }

        ActivityTypeResponse response = activityTypeService.createType(requestObj);
        return Result.success(response, "类型创建成功");
    }

    @GetMapping
    @ApiOperation("获取所有活动类型")
    public Result<List<ActivityTypeResponse>> getAllTypes() {
        List<ActivityTypeResponse> types = activityTypeService.getAllTypes();
        return Result.success(types);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取活动类型详情")
    public Result<ActivityTypeResponse> getTypeById(@PathVariable Long id) {
        ActivityTypeResponse response = activityTypeService.getTypeById(id);
        return Result.success(response);
    }

    /**
     * 更新活动类型
     * 仅允许管理员更新活动类型
     */
    @PutMapping("/{id}")
    @ApiOperation("更新活动类型")
    public Result<ActivityTypeResponse> updateType(
            HttpServletRequest request,
            @PathVariable Long id,
            @Validated({UpdateGroup.class}) @RequestBody ActivityTypeCreateRequest requestObj) {
        String currentUserRole = (String) request.getAttribute("currentUserRole");
        if (currentUserRole == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        if (!UserRoleConstants.ADMIN.equals(currentUserRole)) {
            return Result.error(ResultCode.FORBIDDEN, "只有管理员才能更新活动类型");
        }

        ActivityTypeResponse response = activityTypeService.updateType(id, requestObj);
        return Result.success(response, "类型更新成功");
    }

    /**
     * 删除活动类型
     * 仅允许管理员删除活动类型
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除活动类型")
    public Result<Void> deleteType(
            HttpServletRequest request,
            @PathVariable Long id) {
        String currentUserRole = (String) request.getAttribute("currentUserRole");
        if (currentUserRole == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        if (!UserRoleConstants.ADMIN.equals(currentUserRole)) {
            return Result.error(ResultCode.FORBIDDEN, "只有管理员才能删除活动类型");
        }

        activityTypeService.deleteType(id);
        return Result.success(null, "类型删除成功");
    }
}
