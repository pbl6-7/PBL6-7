package com.campus.activity.controller;

import com.campus.activity.dto.RegistrationPageResponse;
import com.campus.activity.dto.RegistrationRequest;
import com.campus.activity.dto.RegistrationResponse;
import com.campus.activity.dto.RegistrationStatusUpdateRequest;
import com.campus.activity.service.RegistrationService;
import com.campus.core.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/registrations")
@RequiredArgsConstructor
@Api(tags = "活动报名")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    @ApiOperation("报名活动")
    public Result<RegistrationResponse> registerForActivity(
            HttpServletRequest request,
            @Valid @RequestBody RegistrationRequest regRequest) {
        Long userId = (Long) request.getAttribute("currentUserId");
        RegistrationResponse response = registrationService.registerForActivity(userId, regRequest.getActivityId());
        return Result.success(response, "报名成功");
    }

    @GetMapping("/my")
    @ApiOperation("获取我的报名记录")
    public Result<RegistrationPageResponse> getMyRegistrations(
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        Long userId = (Long) request.getAttribute("currentUserId");
        RegistrationPageResponse response = registrationService.getMyRegistrations(userId, page, size);
        return Result.success(response);
    }

    @GetMapping("/activity/{activityId}")
    @ApiOperation("获取活动的报名人员列表（活动发布者）")
    public Result<RegistrationPageResponse> getActivityRegistrations(
            HttpServletRequest request,
            @PathVariable Long activityId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        Long publisherId = (Long) request.getAttribute("currentUserId");
        RegistrationPageResponse response = registrationService.getActivityRegistrations(publisherId, activityId, page, size);
        return Result.success(response);
    }

    @PutMapping("/status")
    @ApiOperation("更新报名状态（活动发布者）")
    public Result<RegistrationResponse> updateRegistrationStatus(
            HttpServletRequest request,
            @Valid @RequestBody RegistrationStatusUpdateRequest statusRequest) {
        Long publisherId = (Long) request.getAttribute("currentUserId");
        RegistrationResponse response = registrationService.updateRegistrationStatus(
                publisherId, statusRequest.getRegistrationId(), statusRequest.getStatus());
        return Result.success(response, "状态更新成功");
    }

    @DeleteMapping("/activity/{activityId}")
    @ApiOperation("取消报名")
    public Result<Void> cancelRegistration(
            HttpServletRequest request,
            @PathVariable Long activityId) {
        Long userId = (Long) request.getAttribute("currentUserId");
        registrationService.cancelRegistration(userId, activityId);
        return Result.success(null, "取消报名成功");
    }
}
