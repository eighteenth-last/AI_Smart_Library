package com.library.module.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI会话管理实体类
 */
@TableName("ai_session")
@Data
public class AiSession {

    /**
     * 会话ID
     */
    @TableId(type = IdType.INPUT)
    private String sessionId;

    /**
     * 用户ID（匿名为NULL）
     */
    private Long userId;

    /**
     * 用户角色
     */
    private String userRole;

    /**
     * 会话标题
     */
    private String sessionTitle;

    /**
     * 会话类型：general/book_recommend/qa
     */
    private String sessionType;

    /**
     * 上下文窗口大小
     */
    private Integer contextWindow;

    /**
     * 上下文消息数组（JSON格式）
     */
    private String contextMessages;

    /**
     * 总消息数
     */
    private Integer totalMessages;

    /**
     * 会话总Token数
     */
    private Integer totalTokens;

    /**
     * 状态：active/archived/deleted
     */
    private String status;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveAt;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 逻辑删除
     */
    private Integer deleted;
}
