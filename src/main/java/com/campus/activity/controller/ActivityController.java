package com.campus.activity.controller;

import com.campus.activity.dto.ActivityPageResponse;
import com.campus.activity.dto.ActivityPublishRequest;
import com.campus.activity.dto.ActivityQueryRequest;
import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.dto.AlbumResponse;
import com.campus.activity.mapper.ActivityShareMapper;
import com.campus.activity.service.ActivityAlbumService;
import com.campus.activity.service.ActivityService;
import com.campus.activity.service.CacheService;
import com.campus.core.common.BusinessException;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import com.campus.core.validation.group.CreateGroup;
import com.campus.core.validation.group.UpdateGroup;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/activities")
@RequiredArgsConstructor
@Api(tags = "活动管理")
public class ActivityController {

    private final ActivityService activityService;
    private final CacheService cacheService;
    private final ActivityAlbumService activityAlbumService;
    private final ActivityShareMapper activityShareMapper;

    @PostMapping
    @ApiOperation("发布活动")
    public Result<ActivityResponse> publishActivity(
            HttpServletRequest request,
            @Validated({CreateGroup.class}) @RequestBody ActivityPublishRequest publishRequest) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        String userRole = (String) request.getAttribute("currentUserRole");
        ActivityResponse response = activityService.publishActivity(userId, userRole, publishRequest);
        return Result.success(response, "活动发布成功");
    }

    @GetMapping("/{id}")
    @ApiOperation("获取活动详情")
    public Result<ActivityResponse> getActivityById(@PathVariable Long id) {
        ActivityResponse response = activityService.getActivityById(id);
        return Result.success(response);
    }

    @GetMapping("/my")
    @ApiOperation("获取我发布的活动列表")
    public Result<List<ActivityResponse>> getMyActivities(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        List<ActivityResponse> activities = activityService.getActivitiesByPublisher(userId);
        return Result.success(activities);
    }

    @PutMapping("/{id}")
    @ApiOperation("编辑活动")
    public Result<ActivityResponse> updateActivity(
            HttpServletRequest request,
            @PathVariable Long id,
            @Validated({UpdateGroup.class}) @RequestBody ActivityPublishRequest updateRequest) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        ActivityResponse response = activityService.updateActivity(id, userId, updateRequest);
        return Result.success(response, "活动更新成功");
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除活动")
    public Result<Void> deleteActivity(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        activityService.deleteActivity(id, userId);
        return Result.success(null, "活动删除成功");
    }

    @GetMapping("/list")
    @ApiOperation("获取活动列表（带筛选和分页）")
    public Result<ActivityPageResponse> getActivityList(
            HttpServletRequest request,
            @ModelAttribute ActivityQueryRequest queryRequest) {
        Long userId = (Long) request.getAttribute("currentUserId");
        ActivityPageResponse response = activityService.getActivityList(userId, queryRequest);
        return Result.success(response);
    }

    @GetMapping
    @ApiOperation("获取活动列表（根路径，兼容/api/v1/activities访问）")
    public Result<ActivityPageResponse> getActivityListRoot(
            HttpServletRequest request,
            @ModelAttribute ActivityQueryRequest queryRequest) {
        return getActivityList(request, queryRequest);
    }

    /**
     * 获取活动状态
     */
    @GetMapping("/{id}/status")
    @ApiOperation("获取活动状态")
    public Result<Map<String, Object>> getActivityStatus(@PathVariable Long id) {
        ActivityResponse activity = activityService.getActivityById(id);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("status", activity.getStatus());
        data.put("approvalStatus", activity.getApprovalStatus());
        return Result.success(data);
    }

    /**
     * 更新活动状态（通用接口）
     */
    @PutMapping("/{id}/status")
    @ApiOperation("更新活动状态")
    public Result<ActivityResponse> updateActivityStatus(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        String status = body.get("status");
        if (status == null || status.trim().isEmpty()) {
            return Result.error(ResultCode.BAD_REQUEST, "状态不能为空");
        }
        ActivityResponse response = activityService.updateActivityStatus(id, userId, status);
        return Result.success(response, "活动状态更新成功");
    }

    /**
     * 发布活动（将草稿活动发布）
     */
    @PutMapping("/{id}/publish")
    @ApiOperation("发布活动")
    public Result<ActivityResponse> publishActivityStatus(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        ActivityResponse response = activityService.updateActivityStatus(id, userId, "published");
        return Result.success(response, "活动已发布");
    }

    /**
     * 取消活动
     */
    @PutMapping("/{id}/cancel")
    @ApiOperation("取消活动")
    public Result<ActivityResponse> cancelActivity(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        ActivityResponse response = activityService.updateActivityStatus(id, userId, "cancelled");
        return Result.success(response, "活动已取消");
    }

    /**
     * 结束活动
     */
    @PutMapping("/{id}/end")
    @ApiOperation("结束活动")
    public Result<ActivityResponse> endActivity(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        ActivityResponse response = activityService.updateActivityStatus(id, userId, "ended");
        return Result.success(response, "活动已结束");
    }

    /**
     * 分享活动（记录分享行为到数据库）
     */
    @PostMapping("/{id}/share")
    @ApiOperation("分享活动")
    public Result<Map<String, Object>> shareActivity(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }

        // 检查是否已分享过
        Integer existing = activityShareMapper.checkUserShared(id, userId);
        if (existing != null && existing > 0) {
            // 已分享过，直接返回计数
            Long shareCount = activityShareMapper.countByActivityId(id);
            Map<String, Object> data = new HashMap<>();
            data.put("activityId", id);
            data.put("shareTime", System.currentTimeMillis());
            data.put("shareUrl", "/api/v1/activities/" + id);
            data.put("shareCount", shareCount);
            data.put("alreadyShared", true);
            return Result.success(data, "您已分享过该活动");
        }

        // 记录分享行为到数据库
        activityShareMapper.insertShare(id, userId);
        Long shareCount = activityShareMapper.countByActivityId(id);

        Map<String, Object> data = new HashMap<>();
        data.put("activityId", id);
        data.put("shareTime", System.currentTimeMillis());
        data.put("shareUrl", "/api/v1/activities/" + id);
        data.put("shareCount", shareCount);
        data.put("alreadyShared", false);
        return Result.success(data, "分享成功");
    }

    /**
     * 获取活动分享次数
     */
    @GetMapping("/{id}/share-count")
    @ApiOperation("获取活动分享次数")
    public Result<Map<String, Object>> getShareCount(@PathVariable Long id) {
        Long shareCount = activityShareMapper.countByActivityId(id);
        Map<String, Object> data = new HashMap<>();
        data.put("activityId", id);
        data.put("shareCount", shareCount != null ? shareCount : 0);
        return Result.success(data);
    }

    /**
     * 获取活动图片列表
     */
    @GetMapping("/{id}/images")
    @ApiOperation("获取活动图片列表")
    public Result<List<AlbumResponse>> getActivityImages(@PathVariable Long id) {
        List<AlbumResponse> albums = activityAlbumService.getAlbumsByActivityId(id);
        return Result.success(albums);
    }

    /**
     * 上传活动图片
     */
    @PostMapping("/{id}/images")
    @ApiOperation("上传活动图片")
    public Result<AlbumResponse> uploadActivityImage(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "sortOrder", defaultValue = "0") Integer sortOrder) {
        Long userId = (Long) request.getAttribute("currentUserId");
        String userRole = (String) request.getAttribute("currentUserRole");
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        if (file.isEmpty()) {
            return Result.error(ResultCode.BAD_REQUEST, "请选择要上传的图片");
        }
        try {
            String uploadPath = "uploads/albums";
            java.io.File uploadDir = new java.io.File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = id + "_" + System.currentTimeMillis() + extension;
            java.io.File dest = new java.io.File(uploadDir, filename);
            file.transferTo(dest);
            String imageUrl = "/uploads/albums/" + filename;
            AlbumResponse response = activityAlbumService.addAlbum(id, imageUrl, null, description, sortOrder, userId, userRole);
            return Result.success(response, "图片上传成功");
        } catch (Exception e) {
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR, "图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除活动图片
     */
    @DeleteMapping("/{id}/images/{imageId}")
    @ApiOperation("删除活动图片")
    public Result<Void> deleteActivityImage(
            HttpServletRequest request,
            @PathVariable Long id,
            @PathVariable Long imageId) {
        Long userId = (Long) request.getAttribute("currentUserId");
        String userRole = (String) request.getAttribute("currentUserRole");
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "请先登录");
        }
        activityAlbumService.deleteAlbum(imageId, userId, userRole);
        return Result.success(null, "图片删除成功");
    }
}
