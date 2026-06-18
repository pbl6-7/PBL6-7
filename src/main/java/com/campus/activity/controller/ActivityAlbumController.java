package com.campus.activity.controller;

import com.campus.activity.dto.AlbumResponse;
import com.campus.activity.service.ActivityAlbumService;
import com.campus.activity.util.FileUploadUtil;
import com.campus.core.common.Result;
import com.campus.core.common.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/albums")
@RequiredArgsConstructor
@Api(tags = "活动相册管理")
@Slf4j
public class ActivityAlbumController {

    private final ActivityAlbumService albumService;
    private final FileUploadUtil fileUploadUtil;

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @GetMapping("/activities/{activityId}")
    @ApiOperation("获取活动相册")
    public Result<List<AlbumResponse>> getAlbumsByActivity(@PathVariable Long activityId) {
        List<AlbumResponse> albums = albumService.getAlbumsByActivityId(activityId);
        return Result.success(albums);
    }

    @PostMapping("/upload")
    @ApiOperation("上传相册图片")
    public Result<AlbumResponse> uploadAlbum(
            @RequestParam("file") MultipartFile file,
            @RequestParam("activityId") Long activityId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "sortOrder", required = false, defaultValue = "0") Integer sortOrder,
            HttpServletRequest request) {
        
        log.info("=== 上传请求开始 ===");
        log.info("activityId: {}, file: {}, fileSize: {}", activityId, file != null ? file.getOriginalFilename() : "null", file != null ? file.getSize() : 0);
        
        if (file == null || file.isEmpty()) {
            log.warn("文件为空");
            return Result.error(ResultCode.VALIDATION_ERROR, "请选择要上传的图片");
        }

        Long userId = (Long) request.getAttribute("currentUserId");
        String userRole = (String) request.getAttribute("currentUserRole");
        log.info("userId from request: {}, userRole: {}", userId, userRole);
        
        if (userId == null) {
            log.warn("用户未登录");
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        try {
            String relativePath = fileUploadUtil.uploadImage(file);
            log.info("文件上传成功，路径: {}", relativePath);
            String thumbnailUrl = relativePath;
            
            AlbumResponse response = albumService.addAlbum(
                activityId, relativePath, thumbnailUrl, description, sortOrder, userId, userRole);
            log.info("相册记录创建成功: {}", response.getId());
            
            return Result.success(response, "上传成功");
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR, "文件上传失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("参数验证失败: {}", e.getMessage());
            return Result.error(ResultCode.VALIDATION_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("上传异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR, "上传失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{albumId}")
    @ApiOperation("删除相册图片")
    public Result<Void> deleteAlbum(
            @PathVariable Long albumId,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("currentUserId");
        String userRole = (String) request.getAttribute("currentUserRole");
        
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        try {
            albumService.deleteAlbum(albumId, userId, userRole);
            return Result.success(null, "删除成功");
        } catch (Exception e) {
            return Result.error(ResultCode.BAD_REQUEST, e.getMessage());
        }
    }
}
