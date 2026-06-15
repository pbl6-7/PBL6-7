package com.campus.activity.service;

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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 活动标签服务类
 * 提供标签的创建、查询、删除以及活动标签关联管理功能
 */
@Service
@RequiredArgsConstructor
public class ActivityTagService {

    private static final int MAX_TAGS_PER_ACTIVITY = 5;
    private static final int MAX_TAG_NAME_LENGTH = 20;

    private final ActivityTagMapper activityTagMapper;
    private final ActivityMapper activityMapper;

    /**
     * 为活动创建标签
     * @param activityId 活动ID
     * @param tagNames 标签名称列表
     * @return 创建的标签响应列表
     */
    @Transactional
    public List<TagResponse> createTagsForActivity(Long activityId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }

        if (tagNames.size() > MAX_TAGS_PER_ACTIVITY) {
            throw new BusinessException(
                    ResultCode.VALIDATION_ERROR,
                    "每个活动最多添加" + MAX_TAGS_PER_ACTIVITY + "个标签");
        }

        List<ActivityTag> tags = new ArrayList<>();
        for (String name : tagNames) {
            if (name == null || name.trim().isEmpty()) {
                continue;
            }
            String trimmedName = name.trim();
            if (trimmedName.length() > MAX_TAG_NAME_LENGTH) {
                throw new BusinessException(
                        ResultCode.VALIDATION_ERROR,
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

    /**
     * 根据活动ID获取标签列表
     * @param activityId 活动ID
     * @return 标签响应列表
     */
    public List<TagResponse> getTagsByActivityId(Long activityId) {
        List<ActivityTag> tags = activityTagMapper.selectByActivityId(activityId);
        return tags.stream()
                .map(TagResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 创建新标签
     * @param request 标签创建请求
     * @return 标签响应
     */
    @Transactional
    public TagResponse createTag(TagCreateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "标签名称不能为空");
        }

        String trimmedName = request.getName().trim();
        if (trimmedName.length() > MAX_TAG_NAME_LENGTH) {
            throw new BusinessException(
                    ResultCode.VALIDATION_ERROR,
                    "标签名称长度不能超过" + MAX_TAG_NAME_LENGTH + "个字符");
        }

        // 检查标签是否已存在
        ActivityTag existingTag = activityTagMapper.selectByName(trimmedName);
        if (existingTag != null) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "标签已存在");
        }

        ActivityTag tag = new ActivityTag();
        tag.setName(trimmedName);
        tag.setColor(request.getColor());
        activityTagMapper.insert(tag);

        return TagResponse.fromEntity(tag);
    }

    /**
     * 获取所有标签
     * @return 标签响应列表
     */
    public List<TagResponse> getAllTags() {
        List<ActivityTag> tags = activityTagMapper.selectAll();
        return tags.stream()
                .map(TagResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取标签
     * @param id 标签ID
     * @return 标签响应
     */
    public TagResponse getTagById(Long id) {
        ActivityTag tag = activityTagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }
        return TagResponse.fromEntity(tag);
    }

    /**
     * 删除标签
     * @param id 标签ID
     */
    @Transactional
    public void deleteTag(Long id) {
        ActivityTag tag = activityTagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }
        activityTagMapper.deleteById(id);
    }

    /**
     * 更新活动的标签（通过标签名称）
     * 先删除旧标签，再创建新标签
     * @param activityId 活动ID
     * @param tagNames 标签名称列表
     * @return 更新后的标签响应列表
     */
    @Transactional
    public List<TagResponse> updateTagsForActivity(Long activityId, List<String> tagNames) {
        // 先删除活动原有的标签
        deleteTagsByActivityId(activityId);
        
        // 再创建新的标签
        return createTagsForActivity(activityId, tagNames);
    }

    /**
     * 删除活动的所有标签
     * @param activityId 活动ID
     */
    @Transactional
    public void deleteTagsByActivityId(Long activityId) {
        activityTagMapper.deleteByActivityId(activityId);
    }

    /**
     * 为活动设置标签
     * 仅允许活动发布者或管理员操作
     * @param activityId 活动ID
     * @param tagIds 标签ID列表
     * @param userId 用户ID
     * @param userRole 用户角色
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

        if (tagIds.size() > MAX_TAGS_PER_ACTIVITY) {
            throw new BusinessException(
                    ResultCode.VALIDATION_ERROR,
                    "每个活动最多添加" + MAX_TAGS_PER_ACTIVITY + "个标签");
        }

        activityTagMapper.deleteRelationByActivityId(activityId);

        for (Long tagId : tagIds) {
            ActivityTag tag = activityTagMapper.selectById(tagId);
            if (tag == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在: " + tagId);
            }
            activityTagMapper.insertRelation(activityId, tag.getName(), tag.getColor());
        }
    }
}