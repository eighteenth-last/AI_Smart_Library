package com.library.module.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.module.ai.entity.TokenUsageStats;
import org.apache.ibatis.annotations.Mapper;

/**
 * Token 使用统计 Mapper 接口
 */
@Mapper
public interface TokenUsageStatsMapper extends BaseMapper<TokenUsageStats> {

}
