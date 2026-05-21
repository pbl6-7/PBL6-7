package com.campus.activity.service;

import com.campus.activity.dto.ActivityPhotoResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.ActivityPhoto;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.ActivityPhotoMapper;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityPhotoService {

    private final ActivityPhotoMapper activityPhotoMapper;
    private final ActivityMapper activityMapper;

    private static final String PHOTO_STORAGE_PATH = "./uploads/activity_photos";
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "请选择要上传的照片");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isValidExtension(originalFilename)) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "不支持的图片格式，仅支持 jpg、jpeg、png、gif、webp");
        }

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "图片大小不能超过10MB");
        }
    }

    private boolean isValidExtension(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    private void validateActivityOwner(Long activityId, Long userId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }
        if (!activity.getPublisherId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权上传此活动的照片");
        }
    }

    @Transactional
    public ActivityPhotoResponse uploadPhoto(Long activityId, Long userId, MultipartFile file) {
        validateActivityOwner(activityId, userId);
        validateFile(file);

        try {
            Path storagePath = Paths.get(PHOTO_STORAGE_PATH);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;
            Path filePath = storagePath.resolve(newFilename);

            Files.copy(file.getInputStream(), filePath);

            String photoUrl = "/api/v1/photos/" + newFilename;

            ActivityPhoto photo = new ActivityPhoto();
            photo.setActivityId(activityId);
            photo.setPhotoUrl(photoUrl);
            photo.setPhotoName(originalFilename);
            photo.setUploadedBy(userId);

            activityPhotoMapper.insert(photo);

            return ActivityPhotoResponse.fromEntity(photo);
        } catch (IOException e) {
            throw new BusinessException(ResultCode.INTERNAL_SERVER_ERROR, "图片上传失败");
        }
    }

    @Transactional
    public List<ActivityPhotoResponse> uploadPhotos(Long activityId, Long userId, List<MultipartFile> files) {
        validateActivityOwner(activityId, userId);

        return files.stream()
                .map(file -> uploadPhoto(activityId, userId, file))
                .collect(Collectors.toList());
    }

    public List<ActivityPhotoResponse> getPhotosByActivityId(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        List<ActivityPhoto> photos = activityPhotoMapper.selectByActivityId(activityId);
        return photos.stream()
                .map(ActivityPhotoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePhoto(Long photoId, Long userId) {
        ActivityPhoto photo = activityPhotoMapper.selectById(photoId);
        if (photo == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "照片不存在");
        }

        validateActivityOwner(photo.getActivityId(), userId);

        try {
            String filename = photo.getPhotoUrl().substring(photo.getPhotoUrl().lastIndexOf("/") + 1);
            Path filePath = Paths.get(PHOTO_STORAGE_PATH).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
        }

        activityPhotoMapper.deleteById(photoId);
    }
}