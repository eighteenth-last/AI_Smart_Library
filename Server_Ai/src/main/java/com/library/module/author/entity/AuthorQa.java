package com.library.module.author.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作者问答实体类
 */
@TableName("author_qa")
@Data
public class AuthorQa {

    /**
     * 问答ID
     */
    @TableId(type = IdType.AUTO)
    private Long qaId;

    /**
     * 作者ID
     */
    private Long authorId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 问题内容
     */
    private String question;

    /**
     * 回答内容
     */
    private String answer;

    /**
     * 状态：pending-待回答, answered-已回答
     */
    private String status;

    /**
     * 是否公开显示
     */
    private Integer isPublic;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 回答时间
     */
    private LocalDateTime answeredAt;
}
