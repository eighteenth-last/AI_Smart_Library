package com.library.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.module.ai.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库Mapper接口
 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {
}
