package com.campus.activity.service;

import com.campus.activity.dto.ActivityTagRequest;
import com.campus.activity.dto.TagCreateRequest;
import com.campus.activity.dto.TagResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.ActivityTag;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.ActivityTagMapper;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityTagService {

    private final ActivityTagMapper activityTagMapper;
    private final ActivityMapper activityMapper;

    @Transactional
    public TagResponse createTag(TagCreateRequest request) {
        ActivityTag existingTag = activityTagMapper.selectByName(request.getName());
        if (existingTag != null) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "标签名称已存在");
        }

        ActivityTag tag = new ActivityTag();
        tag.setName(request.getName());
        tag.setColor(request.getColor());

        activityTagMapper.insert(tag);
        return TagResponse.fromEntity(tag);
    }

    public List<TagResponse> getAllTags() {
        List<ActivityTag> tags = activityTagMapper.selectAll();
        return tags.stream()
                .map(TagResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public TagResponse getTagById(Long id) {
        ActivityTag tag = activityTagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }
        return TagResponse.fromEntity(tag);
    }

    @Transactional
    public void deleteTag(Long id) {
        ActivityTag tag = activityTagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }
        activityTagMapper.deleteById(id);
    }

    public List<TagResponse> getTagsByActivityId(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        List<ActivityTag> tags = activityTagMapper.selectByActivityId(activityId);
        return tags.stream()
                .map(TagResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void setActivityTags(Long activityId, List<Long> tagIds) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
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
