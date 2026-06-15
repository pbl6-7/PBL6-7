package com.campus.activity.service;

import com.campus.activity.dto.AlbumResponse;
import com.campus.activity.entity.ActivityAlbum;
import com.campus.activity.entity.Activity;
import com.campus.activity.mapper.ActivityAlbumMapper;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityAlbumService {
    private final ActivityAlbumMapper albumMapper;
    private final ActivityMapper activityMapper;

    public List<AlbumResponse> getAlbumsByActivityId(Long activityId) {
        return albumMapper.selectByActivityId(activityId).stream()
                .map(AlbumResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public int getAlbumCount(Long activityId) {
        return albumMapper.countByActivityId(activityId);
    }

    @Transactional
    public AlbumResponse addAlbum(Long activityId, String url, String thumbnailUrl, 
                                   String description, Integer sortOrder, Long userId, String userRole) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }
        // 管理员可以上传，任何人不能上传
        if (!"ADMIN".equals(userRole) && !activity.getPublisherId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只有活动发布者或管理员可以添加相册");
        }

        ActivityAlbum album = new ActivityAlbum();
        album.setActivityId(activityId);
        album.setUrl(url);
        album.setThumbnailUrl(thumbnailUrl);
        album.setDescription(description);
        album.setSortOrder(sortOrder != null ? sortOrder : 0);
        album.setCreatedBy(userId);
        albumMapper.insert(album);

        return AlbumResponse.fromEntity(album);
    }

    @Transactional
    public void deleteAlbum(Long albumId, Long userId, String userRole) {
        ActivityAlbum album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "相册图片不存在");
        }

        Activity activity = activityMapper.selectById(album.getActivityId());
        // 管理员可以删除
        if (!"ADMIN".equals(userRole) && (activity == null || !activity.getPublisherId().equals(userId))) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只有活动发布者或管理员可以删除相册图片");
        }

        albumMapper.deleteById(albumId);
    }

    @Transactional
    public void deleteAlbumByAdmin(Long albumId) {
        ActivityAlbum album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "相册图片不存在");
        }
        albumMapper.deleteById(albumId);
    }
}
