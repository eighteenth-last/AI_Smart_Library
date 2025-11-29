package com.library.module.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Token 使用统计实体类
 */
@TableName("token_usage_stats")
@Data
public class TokenUsageStats {

    /**
     * 统计ID
     */
    @TableId(type = IdType.AUTO)
    private Long statId;

    /**
     * 用户ID（NULL表示匿名用户）
     */
    private Long userId;

    /**
     * 用户角色
     */
    private String userRole;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 输入Token
     */
    private Integer inputTokens;

    /**
     * 输出Token
     */
    private Integer outputTokens;

    /**
     * 总Token
     */
    private Integer totalTokens;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 估算成本（元）
     */
    private BigDecimal estimatedCost;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 统计小时（0-23）
     */
    private Integer statHour;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
