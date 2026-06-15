package com.campus.activity.service;

import com.campus.activity.dto.TagCreateRequest;
import com.campus.activity.dto.TagResponse;
import com.campus.activity.entity.Tag;
import com.campus.activity.mapper.TagMapper;
import com.campus.core.common.BusinessException;
import com.campus.core.common.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagMapper tagMapper;

    public List<TagResponse> getAllTags() {
        return tagMapper.selectAll().stream()
                .map(TagResponse::fromTagEntity)
                .collect(Collectors.toList());
    }

    public TagResponse getTagById(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }
        return TagResponse.fromTagEntity(tag);
    }

    @Transactional
    public TagResponse createTag(TagCreateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "标签名称不能为空");
        }
        String name = request.getName().trim();
        Tag existing = tagMapper.selectByName(name);
        if (existing != null) {
            throw new BusinessException(ResultCode.VALIDATION_ERROR, "标签已存在");
        }
        Tag tag = new Tag();
        tag.setName(name);
        tag.setColor(request.getColor());
        tagMapper.insert(tag);
        return TagResponse.fromTagEntity(tag);
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "标签不存在");
        }
        tagMapper.deleteById(id);
    }
}
