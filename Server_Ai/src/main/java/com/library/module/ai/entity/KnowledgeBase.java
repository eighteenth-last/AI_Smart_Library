package com.library.module.ai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库实体类
 */
@TableName("knowledge_base")
@Data
public class KnowledgeBase {

    /**
     * 知识库ID
     */
    @TableId(type = IdType.AUTO)
    private Long kbId;

    /**
     * 问题
     */
    private String question;

    /**
     * 答案
     */
    private String answer;

    /**
     * 分类
     */
    private String category;

    /**
     * 来源：system-系统录入, author-作者回复
     */
    private String source;

    /**
     * 作者ID（如果是作者回复）
     */
    private Long authorId;

    /**
     * 是否公开
     */
    private Integer isPublic;

    /**
     * 命中次数
     */
    private Integer hitCount;

    /**
     * 问题向量（JSON格式）
     */
    private String questionVector;

    /**
     * 向量维度
     */
    private Integer vectorDim;

    /**
     * Embedding模型
     */
    private String embeddingModel;

    /**
     * 标签数组（JSON格式）
     */
    private String tags;

    /**
     * 关联图书ID数组（JSON格式）
     */
    private String bookIds;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除
     */
    private Integer deleted;
}
