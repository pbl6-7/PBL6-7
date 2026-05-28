package com.campus.activity.controller;

import com.campus.activity.dto.TagResponse;
import com.campus.activity.service.ActivityTagService;
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

    private final ActivityTagService activityTagService;

    @GetMapping("/activity/{activityId}")
    @ApiOperation("获取活动的标签")
    public Result<List<TagResponse>> getTagsByActivityId(@PathVariable Long activityId) {
        List<TagResponse> tags = activityTagService.getTagsByActivityId(activityId);
        return Result.success(tags);
    }
}
