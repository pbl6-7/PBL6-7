package com.campus.activity.service;

import com.campus.activity.dto.TopicCreateRequest;
import com.campus.activity.dto.TopicResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.ActivityTopic;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.ActivityTopicMapper;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import com.campus.user.entity.User;
import com.campus.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityTopicService {

    private final ActivityTopicMapper activityTopicMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;

    /**
     * 创建话题
     * 修复问题3：添加权限验证
     */
    @Transactional
    public TopicResponse createTopic(Long creatorId, TopicCreateRequest request, String creatorRole) {
        Activity activity = activityMapper.selectById(request.getActivityId());
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        boolean isPublisher = activity.getPublisherId().equals(creatorId);
        boolean isAdmin = "admin".equals(creatorRole);

        if (!isPublisher && !isAdmin) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只有活动发布者或管理员才能创建话题");
        }

        ActivityTopic topic = new ActivityTopic();
        topic.setActivityId(request.getActivityId());
        topic.setTitle(request.getTitle());
        topic.setCreatorId(creatorId);

        activityTopicMapper.insert(topic);

        TopicResponse response = TopicResponse.fromEntity(topic);
        User creator = userMapper.selectById(creatorId);
        if (creator != null) {
            response.setCreatorName(creator.getRealName());
        }
        return response;
    }

    /**
     * 获取所有话题列表
     */
    public List<TopicResponse> getAllTopics() {
        List<ActivityTopic> topics = activityTopicMapper.selectAll();
        return topics.stream()
                .map(this::enrichTopicResponse)
                .collect(Collectors.toList());
    }

    public List<TopicResponse> getTopicsByActivityId(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "活动不存在");
        }

        List<ActivityTopic> topics = activityTopicMapper.selectByActivityId(activityId);
        return topics.stream()
                .map(this::enrichTopicResponse)
                .collect(Collectors.toList());
    }

    public TopicResponse getTopicById(Long id) {
        ActivityTopic topic = activityTopicMapper.selectById(id);
        if (topic == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "话题不存在");
        }
        return enrichTopicResponse(topic);
    }

    @Transactional
    public TopicResponse updateTopic(Long topicId, Long userId, String title) {
        ActivityTopic topic = activityTopicMapper.selectById(topicId);
        if (topic == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "话题不存在");
        }

        if (!topic.getCreatorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权修改此话题");
        }

        topic.setTitle(title);
        activityTopicMapper.updateById(topic);

        return enrichTopicResponse(topic);
    }

    @Transactional
    public void deleteTopic(Long topicId, Long userId) {
        ActivityTopic topic = activityTopicMapper.selectById(topicId);
        if (topic == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "话题不存在");
        }

        if (!topic.getCreatorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此话题");
        }

        activityTopicMapper.deleteById(topicId);
    }

    private TopicResponse enrichTopicResponse(ActivityTopic topic) {
        TopicResponse response = TopicResponse.fromEntity(topic);
        User creator = userMapper.selectById(topic.getCreatorId());
        if (creator != null) {
            response.setCreatorName(creator.getRealName());
        }
        return response;
    }
}
