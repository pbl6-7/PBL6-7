package com.campus.activity.controller;

import com.campus.activity.dto.ActivityPhotoResponse;
import com.campus.activity.service.ActivityPhotoService;
import com.campus.core.common.JwtUtils;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Api(tags = "活动相册")
public class ActivityPhotoController {

    private final ActivityPhotoService activityPhotoService;
    private final JwtUtils jwtUtils;

    private static final String PHOTO_STORAGE_PATH = "./uploads/activity_photos";

    @PostMapping(value = "/activities/{activityId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("上传活动照片")
    public Result<ActivityPhotoResponse> uploadPhoto(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long activityId,
            @RequestParam("file") MultipartFile file) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        ActivityPhotoResponse response = activityPhotoService.uploadPhoto(activityId, userId, file);
        return Result.success(response, "照片上传成功");
    }

    @PostMapping(value = "/activities/{activityId}/photos/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("批量上传活动照片")
    public Result<List<ActivityPhotoResponse>> uploadPhotos(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long activityId,
            @RequestParam("files") List<MultipartFile> files) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        List<ActivityPhotoResponse> responses = activityPhotoService.uploadPhotos(activityId, userId, files);
        return Result.success(responses, "照片批量上传成功");
    }

    @GetMapping("/activities/{activityId}/photos")
    @ApiOperation("获取活动相册照片列表")
    public Result<List<ActivityPhotoResponse>> getActivityPhotos(@PathVariable Long activityId) {
        List<ActivityPhotoResponse> photos = activityPhotoService.getPhotosByActivityId(activityId);
        return Result.success(photos);
    }

    @DeleteMapping("/photos/{photoId}")
    @ApiOperation("删除活动照片")
    public Result<Void> deletePhoto(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long photoId) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        if (!jwtUtils.validateToken(token)) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
        Long userId = jwtUtils.getUserIdFromToken(token);
        activityPhotoService.deletePhoto(photoId, userId);
        return Result.success(null, "照片删除成功");
    }

    @GetMapping("/photos/{filename}")
    @ApiOperation("获取照片资源")
    public ResponseEntity<Resource> getPhoto(@PathVariable String filename) {
        Path filePath = Paths.get(PHOTO_STORAGE_PATH).resolve(filename).normalize();
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = getContentType(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
}