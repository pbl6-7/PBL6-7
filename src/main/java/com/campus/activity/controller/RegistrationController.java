package com.campus.activity.controller;

import com.campus.activity.dto.RegistrationPageResponse;
import com.campus.activity.dto.RegistrationRequest;
import com.campus.activity.dto.RegistrationResponse;
import com.campus.activity.dto.RegistrationStatusUpdateRequest;
import com.campus.activity.service.RegistrationService;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
@Api(tags = "活动报名")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final JwtUtils jwtUtils;

    @PostMapping
    @ApiOperation("报名活动")
    public Result<RegistrationResponse> registerForActivity(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody RegistrationRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        RegistrationResponse response = registrationService.registerForActivity(userId, request.getActivityId());
        return Result.success(response, "报名成功");
    }

    @GetMapping("/my")
    @ApiOperation("获取我的报名记录")
    public Result<RegistrationPageResponse> getMyRegistrations(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        RegistrationPageResponse response = registrationService.getMyRegistrations(userId, page, size);
        return Result.success(response);
    }

    @GetMapping("/activity/{activityId}")
    @ApiOperation("获取活动的报名人员列表（活动发布者）")
    public Result<RegistrationPageResponse> getActivityRegistrations(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long activityId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long publisherId = jwtUtils.getUserIdFromToken(token);
        RegistrationPageResponse response = registrationService.getActivityRegistrations(publisherId, activityId, page, size);
        return Result.success(response);
    }

    @PutMapping("/status")
    @ApiOperation("更新报名状态（活动发布者）")
    public Result<RegistrationResponse> updateRegistrationStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody RegistrationStatusUpdateRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long publisherId = jwtUtils.getUserIdFromToken(token);
        RegistrationResponse response = registrationService.updateRegistrationStatus(
                publisherId, request.getRegistrationId(), request.getStatus());
        return Result.success(response, "状态更新成功");
    }

    @DeleteMapping("/activity/{activityId}")
    @ApiOperation("取消报名")
    public Result<Void> cancelRegistration(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long activityId) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        registrationService.cancelRegistration(userId, activityId);
        return Result.success(null, "取消报名成功");
    }
}
