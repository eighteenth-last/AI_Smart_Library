package com.library.module.tag.service.impl;

import com.library.common.exception.BusinessException;
import com.library.module.tag.entity.Tag;
import com.library.module.tag.mapper.TagMapper;
import com.library.module.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 标签服务实现类
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    
    private final TagMapper tagMapper;
    
    @Override
    public List<Tag> getAllTags() {
        return tagMapper.selectList(null);
    }
    
    @Override
    public void addTag(Tag tag) {
        tag.setCreatedAt(LocalDateTime.now());
        tagMapper.insert(tag);
    }
    
    @Override
    public void updateTag(Tag tag) {
        Tag existing = tagMapper.selectById(tag.getTagId());
        if (existing == null) {
            throw new BusinessException("标签不存在");
        }
        
        tagMapper.updateById(tag);
    }
    
    @Override
    public void deleteTag(Long tagId) {
        tagMapper.deleteById(tagId);
    }
}
