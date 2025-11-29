package com.library.module.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI对话日志实体类
 */
@TableName("ai_chat_log")
@Data
public class AiChatLog {

    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long logId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID（匿名用户为null）
     */
    private Long userId;

    /**
     * 用户角色：reader/author/admin
     */
    private String userRole;

    /**
     * 问题
     */
    private String question;

    /**
     * 答案
     */
    private String answer;

    /**
     * 来源：knowledge_base-知识库, deepseek-Deepseek API
     */
    private String source;

    /**
     * 知识库ID（如果来源是知识库）
     */
    private Long kbId;

    /**
     * 问题向量（JSON格式）
     */
    private String questionVector;

    /**
     * 相似度得分
     */
    private Float similarityScore;

    /**
     * 检索到的文档列表（JSON格式）
     */
    private String retrievedDocs;

    /**
     * 输入Token数
     */
    private Integer inputTokens;

    /**
     * 输出Token数
     */
    private Integer outputTokens;

    /**
     * 总Token数
     */
    private Integer totalTokens;

    /**
     * 使用的模型名称
     */
    private String modelName;

    /**
     * 上下文消息列表（JSON格式）
     */
    private String contextMessages;

    /**
     * 是否启用上下文
     */
    private Boolean isContextEnabled;

    /**
     * 用户反馈：1满意/-1不满意/NULL未反馈
     */
    private Integer userFeedback;

    /**
     * 反馈备注
     */
    private String feedbackComment;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 是否删除（逻辑删除）
     */
    private Integer deleted;
}
