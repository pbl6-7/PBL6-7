package com.campus.activity.service;

import com.campus.activity.dto.TagResponse;
import com.campus.activity.entity.ActivityTag;
import com.campus.activity.mapper.ActivityTagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityTagService {

    private static final int MAX_TAGS_PER_ACTIVITY = 5;
    private static final int MAX_TAG_NAME_LENGTH = 20;

    private final ActivityTagMapper activityTagMapper;

    @Transactional
    public List<TagResponse> createTagsForActivity(Long activityId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }

        if (tagNames.size() > MAX_TAGS_PER_ACTIVITY) {
            throw new com.campus.core.common.BusinessException(
                    com.campus.core.common.ResultCode.VALIDATION_ERROR,
                    "每个活动最多添加" + MAX_TAGS_PER_ACTIVITY + "个标签");
        }

        List<ActivityTag> tags = new ArrayList<>();
        for (String name : tagNames) {
            if (name == null || name.trim().isEmpty()) {
                continue;
            }
            String trimmedName = name.trim();
            if (trimmedName.length() > MAX_TAG_NAME_LENGTH) {
                throw new com.campus.core.common.BusinessException(
                        com.campus.core.common.ResultCode.VALIDATION_ERROR,
                        "标签名称长度不能超过" + MAX_TAG_NAME_LENGTH + "个字符");
            }
            ActivityTag tag = new ActivityTag();
            tag.setActivityId(activityId);
            tag.setName(trimmedName);
            tags.add(tag);
        }

        if (!tags.isEmpty()) {
            activityTagMapper.batchInsert(tags);
        }

        return tags.stream()
                .map(TagResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TagResponse> getTagsByActivityId(Long activityId) {
        List<ActivityTag> tags = activityTagMapper.selectByActivityId(activityId);
        return tags.stream()
                .map(TagResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 为活动设置标签
     * 修复问题2：添加权限验证，仅允许活动发布者或管理员操作
     */
    @Transactional
    public void setActivityTags(Long activityId, List<Long> tagIds, Long userId, String userRole) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        boolean isAdmin = "admin".equals(userRole);
        boolean isPublisher = activity.getPublisherId().equals(userId);

        if (!isAdmin && !isPublisher) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权修改此活动的标签");
        }

        if (tagIds == null || tagIds.isEmpty()) {
            activityTagMapper.deleteRelationByActivityId(activityId);
            return;
        }

        activityTagMapper.deleteRelationByActivityId(activityId);

        for (Long tagId : tagIds) {
            ActivityTag tag = activityTagMapper.selectById(tagId);
            if (tag == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在: " + tagId);
            }
            activityTagMapper.insertRelation(activityId, tagId);
        }
    }
}
