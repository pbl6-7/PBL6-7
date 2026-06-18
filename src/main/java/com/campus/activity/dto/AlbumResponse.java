package com.campus.activity.dto;

import com.campus.activity.entity.ActivityAlbum;
import lombok.Data;

@Data
public class AlbumResponse {
    private Long id;
    private Long activityId;
    private String url;
    private String thumbnailUrl;
    private String description;
    private Integer sortOrder;

    public static AlbumResponse fromEntity(ActivityAlbum album) {
        AlbumResponse response = new AlbumResponse();
        response.setId(album.getId());
        response.setActivityId(album.getActivityId());
        response.setUrl(album.getUrl());
        response.setThumbnailUrl(album.getThumbnailUrl());
        response.setDescription(album.getDescription());
        response.setSortOrder(album.getSortOrder());
        return response;
    }
}
