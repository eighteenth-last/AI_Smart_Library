package com.library.module.ai.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话信息VO
 */
@Data
public class SessionVO {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 会话标题
     */
    private String sessionTitle;

    /**
     * 会话预览（第一条消息）
     */
    private String preview;

    /**
     * 总消息数
     */
    private Integer totalMessages;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
