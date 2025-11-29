package com.library.module.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知消息实体类
 */
@TableName("notification")
@Data
public class Notification {

    /**
     * 通知ID
     */
    @TableId(type = IdType.AUTO)
    private Long notificationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 通知类型：borrow_due/overdue/system_announcement
     */
    private String type;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 关联ID（如借阅记录ID）
     */
    private Long relatedId;

    /**
     * 是否已读：1已读 0未读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
