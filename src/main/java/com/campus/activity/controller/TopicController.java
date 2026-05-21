package com.campus.activity.controller;

import com.campus.activity.dto.TopicCreateRequest;
import com.campus.activity.dto.TopicResponse;
import com.campus.activity.dto.TopicUpdateRequest;
import com.campus.activity.service.ActivityTopicService;
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
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
@Api(tags = "活动话题管理")
public class TopicController {

    private final ActivityTopicService activityTopicService;
    private final JwtUtils jwtUtils;

    @PostMapping
    @ApiOperation("创建话题")
    public Result<TopicResponse> createTopic(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody TopicCreateRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        TopicResponse response = activityTopicService.createTopic(userId, request);
        return Result.success(response, "话题创建成功");
    }

    @GetMapping("/activity/{activityId}")
    @ApiOperation("获取活动的话题列表")
    public Result<List<TopicResponse>> getTopicsByActivityId(@PathVariable Long activityId) {
        List<TopicResponse> topics = activityTopicService.getTopicsByActivityId(activityId);
        return Result.success(topics);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取话题详情")
    public Result<TopicResponse> getTopicById(@PathVariable Long id) {
        TopicResponse response = activityTopicService.getTopicById(id);
        return Result.success(response);
    }

    @PutMapping("/{id}")
    @ApiOperation("更新话题")
    public Result<TopicResponse> updateTopic(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody TopicUpdateRequest request) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        TopicResponse response = activityTopicService.updateTopic(id, userId, request.getTitle());
        return Result.success(response, "话题更新成功");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除话题")
    public Result<Void> deleteTopic(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        activityTopicService.deleteTopic(id, userId);
        return Result.success(null, "话题删除成功");
    }
}
