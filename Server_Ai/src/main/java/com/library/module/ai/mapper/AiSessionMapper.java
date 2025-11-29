package com.library.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.module.ai.entity.AiSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI会话Mapper
 */
@Mapper
public interface AiSessionMapper extends BaseMapper<AiSession> {
}
