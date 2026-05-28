package com.campus.activity.controller;

import com.campus.activity.dto.ActivityTypeCreateRequest;
import com.campus.activity.dto.ActivityTypeResponse;
import com.campus.activity.service.ActivityTypeService;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/activity-types")
@RequiredArgsConstructor
@Api(tags = "活动类型管理")
public class ActivityTypeController {

    private final ActivityTypeService activityTypeService;
    private final JwtUtils jwtUtils;

    @PostMapping
    @ApiOperation("创建活动类型")
    public Result<ActivityTypeResponse> createType(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ActivityTypeCreateRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        ActivityTypeResponse response = activityTypeService.createType(request);
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

    @PutMapping("/{id}")
    @ApiOperation("更新活动类型")
    public Result<ActivityTypeResponse> updateType(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody ActivityTypeCreateRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        ActivityTypeResponse response = activityTypeService.updateType(id, request);
        return Result.success(response, "类型更新成功");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除活动类型")
    public Result<Void> deleteType(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        activityTypeService.deleteType(id);
        return Result.success(null, "类型删除成功");
    }
}
