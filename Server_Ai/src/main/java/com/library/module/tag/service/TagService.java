package com.library.module.tag.service;

import com.library.module.tag.entity.Tag;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService {
    
    /**
     * 获取所有标签
     */
    List<Tag> getAllTags();
    
    /**
     * 新增标签
     */
    void addTag(Tag tag);
    
    /**
     * 更新标签
     */
    void updateTag(Tag tag);
    
    /**
     * 删除标签
     */
    void deleteTag(Long tagId);
}
