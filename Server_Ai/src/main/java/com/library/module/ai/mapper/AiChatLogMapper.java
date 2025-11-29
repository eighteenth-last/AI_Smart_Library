package com.library.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.module.ai.entity.AiChatLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI对话日志Mapper接口
 */
@Mapper
public interface AiChatLogMapper extends BaseMapper<AiChatLog> {
}
