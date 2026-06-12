package com.campus.activity.service;

import com.campus.activity.dto.ActivityTypeCreateRequest;
import com.campus.activity.dto.ActivityTypeResponse;
import com.campus.activity.entity.ActivityType;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.ActivityTypeMapper;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityTypeService {

    private final ActivityTypeMapper activityTypeMapper;
    private final ActivityMapper activityMapper;

    @Transactional
    public ActivityTypeResponse createType(ActivityTypeCreateRequest request) {
        ActivityType existingType = activityTypeMapper.selectByName(request.getName());
        if (existingType != null) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "类型名称已存在");
        }

        ActivityType type = new ActivityType();
        type.setName(request.getName());

        activityTypeMapper.insert(type);
        return ActivityTypeResponse.fromEntity(type);
    }

    public List<ActivityTypeResponse> getAllTypes() {
        List<ActivityType> types = activityTypeMapper.selectAll();
        return types.stream()
                .map(ActivityTypeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ActivityTypeResponse getTypeById(Long id) {
        ActivityType type = activityTypeMapper.selectById(id);
        if (type == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "类型不存在");
        }
        return ActivityTypeResponse.fromEntity(type);
    }

    @Transactional
    public ActivityTypeResponse updateType(Long id, ActivityTypeCreateRequest request) {
        ActivityType type = activityTypeMapper.selectById(id);
        if (type == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "类型不存在");
        }

        ActivityType existingType = activityTypeMapper.selectByName(request.getName());
        if (existingType != null && !existingType.getId().equals(id)) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "类型名称已存在");
        }

        type.setName(request.getName());
        activityTypeMapper.updateById(type);
        return ActivityTypeResponse.fromEntity(type);
    }

    @Transactional
    public void deleteType(Long id) {
        ActivityType type = activityTypeMapper.selectById(id);
        if (type == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "类型不存在");
        }

        Long activityCount = activityMapper.countByTypeId(id);
        if (activityCount != null && activityCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该类型下存在活动，无法删除");
        }

        activityTypeMapper.deleteById(id);
    }
}
