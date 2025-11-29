package com.library.module.author.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作者实体类
 */
@TableName("author")
@Data
public class Author {

    /**
     * 作者ID
     */
    @TableId(type = IdType.AUTO)
    private Long authorId;

    /**
     * 关联的用户ID
     */
    private Long userId;

    /**
     * 作者名称
     */
    private String name;

    /**
     * 国家
     */
    private String country;

    /**
     * 出生年份
     */
    private Integer birthYear;

    /**
     * 作者简介
     */
    private String intro;

    /**
     * 认证状态：0-待审核, 1-已通过, 2-已拒绝
     */
    private Integer authStatus;

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
     * 是否删除（逻辑删除）
     */
    @TableLogic
    private Integer deleted;
}
